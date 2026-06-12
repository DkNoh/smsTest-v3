/**
 * tui-common.js
 * TUI Grid 공통 유틸리티 모듈
 */
const TuiCommon = (() => {

    const fmt = {
        // TUI Grid formatter({value})와 직접 호출(문자열) 양쪽을 지원한다
        // 날짜만(LocalDate)은 YYYY-MM-DD, 일시(LocalDateTime)는 YYYY-MM-DD HH:mm
        date: v => {
            const val = (v && typeof v === 'object' && 'value' in v) ? v.value : v;
            if (!val) return '-';
            const parsed = dayjs(val);
            if (!parsed.isValid()) return String(val);
            return String(val).length > 10
                ? parsed.format('YYYY-MM-DD HH:mm')
                : parsed.format('YYYY-MM-DD');
        },

        sendStatus: ({ value }) => {
            const map = {
                SUCCESS: ['badge-success', '성공'],
                FAIL:    ['badge-fail',    '실패'],
                WAIT:    ['badge-wait',    '대기'],
            };
            const clsAndLabel = map[value] || ['', value || '-'];
            const cls = clsAndLabel[0];
            const label = clsAndLabel[1];
            return `<span class="badge ${cls}">${label}</span>`;
        },

        sendType: ({ value }) => {
            const cls   = { SMS: 'type-sms', LMS: 'type-lms', ALIMTALK: 'type-alimtalk' };
            const label = { SMS: 'SMS',       LMS: 'LMS',      ALIMTALK: '알림톡' };
            return value
                ? `<span class="type-badge ${cls[value] || ''}">${label[value] || value}</span>`
                : '-';
        },

        resendYn: ({ value }) =>
            value === 'Y'
                ? '<span class="resend-y">Y</span>'
                : '<span class="resend-n">N</span>',
    };

    const gridDefaults = {
        rowHeight:     42,
        bodyHeight:    'auto',
        minBodyHeight: 300,
        scrollX:       false,
        scrollY:       false,
    };

    // v3 화면 골격 기준은 id="total-count"다 (screen-convention.md)
    const updateTotalCount = (count, selector = '#total-count') => {
        const el = document.querySelector(selector);
        if (el) el.textContent = Number(count).toLocaleString();
    };

    const renderPagination = (page, totalPages, onMove, paginationId = 'pagination') => {
        const wrap = document.getElementById(paginationId);
        if (!wrap || totalPages <= 0) {
            if (wrap) wrap.innerHTML = '';
            return;
        }

        const BLOCK     = 10;
        const startPage = Math.floor((page - 1) / BLOCK) * BLOCK + 1;
        const endPage   = Math.min(startPage + BLOCK - 1, totalPages);

        const fnName = `__movePage_${paginationId.replace(/-/g, '_')}`;
        
        const btn = (label, p, disabled) =>
            `<button class="page-btn${disabled ? ' disabled' : ''}"
                     ${disabled ? 'disabled' : `onclick="${fnName}(${p})"`}>${label}</button>`;

        let html = btn('«', 1,             startPage === 1);
        html    += btn('‹', startPage - 1, startPage === 1);
        for (let p = startPage; p <= endPage; p++) {
            html += `<button class="page-btn${p === page ? ' active' : ''}"
                             onclick="${fnName}(${p})">${p}</button>`;
        }
        html += btn('›', endPage + 1, endPage === totalPages);
        html += btn('»', totalPages,  endPage === totalPages);

        wrap.innerHTML    = html;
        window[fnName] = onMove; 
    };

    const exportExcel = (gridObj, fileName = 'download') => {
        if (!gridObj) return;
        gridObj.export('xlsx', { fileName: fileName });
    };

    return {
        fmt,
        gridDefaults,
        updateTotalCount,
        renderPagination,
        exportExcel,
    };
})();
