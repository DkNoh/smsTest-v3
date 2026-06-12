/**
 * @class TuiPageBuilder
 * @description 
 * 단순 CRUD(목록 조회 위주) 백오피스 화면에서 반복적으로 작성되는 TUI Grid 초기화, 페이징 로직, 비동기 통신(fetch), 
 * 그리고 공통 버튼 이벤트(조회, 초기화, 페이지 사이즈 변경)를 단일 진입점(Facade)에서 자동화해 주는 래퍼(Wrapper) 엔진입니다.
 * 이 클래스를 사용하면 각 화면별 JS 파일에서는 고유한 컬럼 정의와 상세 모달 로직만 작성하면 됩니다.
 */
class TuiPageBuilder {
    
    /**
     * TuiPageBuilder 인스턴스를 생성하고 내부 구성 요소들을 초기화합니다.
     * @param {Object} config - 화면별 고유 설정값
     * @param {string} config.el - TUI Grid가 렌더링될 DOM 요소의 ID (기본값: 'grid')
     * @param {string} config.apiUrl - 데이터를 조회할 백엔드 API URL (필수)
     * @param {Array<string>} config.searchInputs - 검색 조건으로 사용할 HTML input/select 요소들의 ID 배열
     * @param {Array<string>} config.rowHeaders - 그리드 좌측 헤더 설정 (예: ['rowNum'], 다중선택시 ['checkbox', 'rowNum'])
     * @param {Array<Object>} config.columns - TUI Grid의 컬럼 메타데이터 배열
     * @param {string} config.pageSizeEl - 페이지당 건수를 조절하는 select 요소의 ID (기본값: 'pageSizeSelect')
     * @param {string} config.btnSearch - 검색 조회 버튼의 ID (기본값: 'btn-search')
     * @param {string} config.btnReset - 검색 조건 초기화 버튼의 ID (기본값: 'btn-reset')
     * @param {Function} config.onGridDblClick - 그리드 행 더블클릭 시 실행될 콜백 함수 (행 데이터 반환)
     * @param {Function} config.onGridUpdated - 데이터 갱신이 완료된 후 실행될 콜백 함수
     */
    constructor(config) {
        // 1. 기본 설정값과 사용자가 넘겨준 config를 병합(Merge)합니다.
        this.config = Object.assign({
            el: 'grid',               
            apiUrl: '',               
            searchInputs: [],         
            requiredInputs: [],       
            rowHeaders: ['rowNum'],   
            columns: [],              
            pageSizeEl: 'pageSizeSelect',
            btnSearch: 'btn-search',
            btnReset: 'btn-reset',
            paginationId: 'pagination',
            totalCountSelector: '#total-count',
            gridOptions: {},          // 화면별 그리드 옵션 예외 (gridDefaults 위에 덮어씀)
            onGridUpdated: null,
            // [API 단건 조회(패턴B) 옵션]
            detailApiUrl: null,       // 예: '/sample/detail'
            detailParamKey: null,     // 예: 'empId'
            onDetailLoaded: null,     // API 통신 성공 후 DTO를 받을 콜백
            // [단순 뷰어(패턴C) 자동 생성 옵션]
            autoModal: false,         // true 설정 시 그리드 컬럼 메타를 이용해 자동 모달 띄움
            autoModalTitle: '상세 정보'
        }, config);

        // 2. 내부 상태 변수 초기화
        this.grid = null;           
        this.currentPage = 1;       
        this.currentSize = 10;      

        // 3. 페이지 로드 시 콤보박스 동기화
        const sizeEl = document.getElementById(this.config.pageSizeEl);
        if (sizeEl) this.currentSize = parseInt(sizeEl.value, 10);

        // 4. 날짜 기본 세팅
        if (typeof CommonUtils !== 'undefined' && typeof CommonUtils.setDefaultDateTime === 'function') {
            CommonUtils.setDefaultDateTime();
        }

        // 5. 그리드 렌더링 및 이벤트 바인딩
        this._initGrid();
        this._bindEvents();

        // 6. 렌더링 완료 후 1페이지 자동 조회 실행
        this.searchData(1);
    }

    _initGrid() {
        // 그리드 공통 옵션은 TuiCommon.gridDefaults가 단일 통제점이다.
        // 화면별 예외는 config.gridOptions로 넘긴다.
        const defaults = (typeof TuiCommon !== 'undefined' && TuiCommon.gridDefaults)
            ? TuiCommon.gridDefaults
            : { scrollX: false, scrollY: false, minBodyHeight: 300 };

        this.grid = new tui.Grid(Object.assign({}, defaults, this.config.gridOptions, {
            el: document.getElementById(this.config.el),
            rowHeaders: this.config.rowHeaders,
            columns: this.config.columns
        }));

        // 더블클릭 이벤트 바인딩 (하이브리드 모달 로직 적용)
        this.grid.on('dblclick', (ev) => {
            if (ev.rowKey !== null && ev.rowKey !== undefined) {
                const row = this.grid.getRow(ev.rowKey);
                
                // [패턴 B] API 단건 조회 모드 (HTTP 호출은 axios로 통일한다)
                if (this.config.detailApiUrl && this.config.detailParamKey && this.config.onDetailLoaded) {
                    const paramVal = row[this.config.detailParamKey];
                    if (!paramVal) return;
                    axios.get(this.config.detailApiUrl, { params: { [this.config.detailParamKey]: paramVal } })
                         .then(res => this.config.onDetailLoaded(res.data))
                         .catch(err => console.error('API 상세 조회 실패', err));
                }
                // [패턴 C] 컬럼 메타데이터 기반 단순 뷰어 자동 생성 모드
                else if (this.config.autoModal) {
                    this._showAutoModal(row);
                }
                // [패턴 A] 사용자 정의 콜백 모드
                else if (this.config.onGridDblClick) {
                    this.config.onGridDblClick(row);
                }
            }
        });
    }

    /**
     * [내부 메서드] TUI Grid 컬럼 정의를 기반으로 자동 팝업 DOM을 생성하고 데이터를 바인딩합니다.
     */
    _showAutoModal(row) {
        let modal = document.getElementById('tui-auto-modal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'tui-auto-modal';
            modal.className = 'modal-overlay';
            modal.innerHTML = `
                <div class="modal-box">
                    <div class="modal-header">
                        <h3 id="tui-auto-modal-title"></h3>
                        <button type="button" class="modal-close" onclick="document.getElementById('tui-auto-modal').classList.remove('open')">&times;</button>
                    </div>
                    <div class="modal-body" id="tui-auto-modal-body" style="max-height: 70vh; overflow-y: auto;">
                        <!-- 자동 주입 -->
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="document.getElementById('tui-auto-modal').classList.remove('open')">닫기</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        }

        document.getElementById('tui-auto-modal-title').textContent = this.config.autoModalTitle;
        
        const bodyEl = document.getElementById('tui-auto-modal-body');
        bodyEl.innerHTML = ''; // 초기화

        // 컬럼 정의(this.config.columns)를 순회하며 행 데이터를 바인딩
        this.config.columns.forEach(col => {
            // hidden 컬럼이거나 불필요한 필드는 제외하고 싶다면 col.hidden 체크를 추가할 수 있습니다.
            if (col.hidden) return;
            
            const rawVal = row[col.name];
            // formatter가 있으면 실행한 결과를 HTML로 넣고, 없으면 텍스트로
            const valHTML = col.formatter && typeof col.formatter === 'function' 
                            ? col.formatter({ value: rawVal, row: row }) 
                            : (rawVal || '-');

            const rowDiv = document.createElement('div');
            rowDiv.className = 'detail-row';
            rowDiv.innerHTML = `
                <div class="detail-label">${col.header}</div>
                <div class="detail-value">${valHTML}</div>
            `;
            bodyEl.appendChild(rowDiv);
        });

        // 애니메이션 효과와 함께 모달 열기
        modal.classList.add('open');
    }

    /**
     * [퍼블릭 메서드] 설정된 API URL로 검색 조건과 페이징 정보를 담아 비동기 조회(Fetch)를 수행합니다.
     * @param {number} page - 조회할 페이지 번호 (기본값: 1)
     */
    searchData(page = 1) {
        // 0. 필수값 검증 (Validation)
        if (this.config.requiredInputs && this.config.requiredInputs.length > 0) {
            for (const id of this.config.requiredInputs) {
                const el = document.getElementById(id);
                if (el && !el.value.trim()) {
                    if (typeof CommonUtils !== 'undefined') {
                        CommonUtils.toast('필수 검색 조건을 입력해 주세요.', 'warning');
                    } else {
                        alert('필수 검색 조건을 입력해 주세요.');
                    }
                    el.focus();
                    return; // 검증 실패 시 API 호출 중단
                }
            }
        }

        // 0.1. 날짜 논리 검증 (시작일 > 종료일 방지)
        const startDateEl = document.getElementById('startDate');
        const endDateEl = document.getElementById('endDate');
        if (startDateEl && endDateEl && startDateEl.value && endDateEl.value) {
            if (dayjs(startDateEl.value).isAfter(dayjs(endDateEl.value))) {
                if (typeof CommonUtils !== 'undefined') {
                    CommonUtils.toast('시작일은 종료일보다 클 수 없습니다.', 'warning');
                } else {
                    alert('시작일은 종료일보다 클 수 없습니다.');
                }
                startDateEl.focus();
                return; // 검증 실패 시 API 호출 중단
            }
        }

        this.currentPage = page; // 현재 페이지 상태 업데이트
        
        // 1. GET 요청 파라미터 조합 (URLSearchParams 활용)
        const params = new URLSearchParams();
        params.append('page', this.currentPage);
        params.append('size', this.currentSize);
        
        // 사용자가 config에 등록한 searchInputs 배열을 순회하며 검색 조건 값을 추출해 파라미터에 추가합니다.
        this.config.searchInputs.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            let value = el.value;
            // 날짜 전송 형식 규약: date -> YYYYMMDD, datetime-local -> YYYYMMDDHHmm (screen-convention.md)
            if (el.type === 'date') {
                value = value ? dayjs(value).format('YYYYMMDD') : '';
            } else if (el.type === 'datetime-local') {
                value = value ? dayjs(value).format('YYYYMMDDHHmm') : '';
            }
            params.append(id, value);
        });

        // 2. 서버 연동 — HTTP 호출은 axios로 통일한다 (screen-convention.md)
        //    전역 인터셉터가 스피너, ApiResponse 언래핑, 오류 모달을 담당한다.
        axios.get(this.config.apiUrl, { params })
        .then(response => {
            const page = response.data; // 인터셉터가 언래핑한 PageResponseDTO

            // 세션 만료 시 로그인 페이지 HTML이 올 수 있다 (ApiResponse가 아니라 언래핑되지 않음)
            if (typeof page === 'string' || !page) {
                if (typeof CommonUtils !== 'undefined') {
                    CommonUtils.toast('세션이 만료되었습니다. 다시 로그인하세요.', 'error');
                }
                return;
            }

            const contents = page.contents || [];
            this.grid.resetData(contents);

            // 데이터가 0건일 때 토스트 알림
            if (contents.length === 0 && typeof CommonUtils !== 'undefined') {
                CommonUtils.toast('조회된 데이터가 없습니다.', 'info');
            }

            // 공통 페이징 함수(TuiCommon)가 로드되어 있다면, 텍스트 갱신 및 페이징 버튼 렌더링 호출
            if (typeof TuiCommon !== 'undefined') {
                TuiCommon.updateTotalCount(page.totalCount || 0, this.config.totalCountSelector);
                TuiCommon.renderPagination(
                    page.page || 1,
                    page.totalPages || 1,
                    (p) => this.searchData(p),
                    this.config.paginationId
                );
            }

            // 외부에서 주입한 데이터 갱신 완료 콜백이 있다면 실행
            if (this.config.onGridUpdated) {
                this.config.onGridUpdated(page);
            }
        })
        .catch(err => {
            // 오류 알림은 common-utils 전역 인터셉터가 모달로 표시한다. 여기서는 기록만 남긴다.
            console.error('[TuiPageBuilder] 목록 조회 실패', err);
        });
    }

    /**
     * [내부 메서드] 화면 내 공통 툴바 버튼들(조회, 초기화, 사이즈 변경)에 이벤트를 바인딩합니다.
     * @private
     */
    _bindEvents() {
        // 1. 조회 버튼 바인딩
        const btnSearch = document.getElementById(this.config.btnSearch);
        if (btnSearch) {
            btnSearch.addEventListener('click', () => this.searchData(1));
        }

        // 2. 초기화 버튼 바인딩
        const btnReset = document.getElementById(this.config.btnReset);
        if (btnReset) {
            btnReset.addEventListener('click', () => {
                // TuiPageBuilder 자체적으로 등록된 검색 조건들을 모두 빈 값으로 초기화합니다.
                this.config.searchInputs.forEach(id => {
                    const el = document.getElementById(id);
                    if (el && el.tagName !== 'SELECT') el.value = '';
                    if (el && el.tagName === 'SELECT') el.selectedIndex = 0;
                });
                
                // 만약 화면 특화 초기화 로직이 있다면 추가 실행
                if (typeof CommonUtils !== 'undefined' && typeof CommonUtils.resetFields === 'function') {
                    CommonUtils.resetFields();
                }
                
                // 초기화 후 1페이지 재조회
                this.searchData(1);
            });
        }

        // 3. 페이지 사이즈(10건, 20건...) 셀렉트 박스 바인딩
        const pageSizeEl = document.getElementById(this.config.pageSizeEl);
        if (pageSizeEl) {
            pageSizeEl.addEventListener('change', (e) => {
                this.currentSize = parseInt(e.target.value, 10);
                this.searchData(1); // 사이즈 변경 시 1페이지로 돌아가서 재조회
            });
        }

        // 4. 검색창 Enter 키 입력 시 자동 조회 바인딩
        if (this.config.searchInputs && this.config.searchInputs.length > 0) {
            this.config.searchInputs.forEach(id => {
                const el = document.getElementById(id);
                if (el && el.tagName === 'INPUT') {
                    el.addEventListener('keypress', (e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault(); // 기본 폼 제출 방지
                            this.searchData(1);
                        }
                    });
                }
            });
        }
    }

    /**
     * 외부(화면별 JS)에서 내부의 TUI Grid 원본 객체에 직접 접근할 필요가 있을 때 호출합니다.
     * @returns {Object} TUI Grid 인스턴스
     */
    getGrid() { return this.grid; }

    /**
     * 좌측 체크박스가 활성화된 행(Row)들의 데이터를 배열로 반환합니다. 일괄 처리(예: 일괄 승인) 시 유용합니다.
     * @returns {Array<Object>} 체크된 행 데이터 배열
     */
    getCheckedRows() { return this.grid.getCheckedRows(); }

    /**
     * 현재 마우스 클릭으로 포커싱된 단일 셀의 정보를 반환합니다. 단건 상세 조회 시 주로 쓰입니다.
     * @returns {Object|null} 포커스된 셀 정보 객체
     */
    getFocusedCell() { return this.grid.getFocusedCell(); }

    /**
     * 현재 유지 중인 페이지 번호를 반환합니다. 데이터 수정/삭제 후 현재 페이지를 재조회할 때 활용합니다.
     * @returns {number} 현재 페이지 번호
     */
    getCurrentPage() { return this.currentPage; }
}
