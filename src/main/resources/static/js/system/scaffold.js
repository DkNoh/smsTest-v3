document.addEventListener('DOMContentLoaded', function () {
    let results = {};
    let currentKey = null;

    document.querySelector('#btn-generate').addEventListener('click', async () => {
        const request = {
            moduleName: document.querySelector('#moduleName').value.trim(),
            domainId: document.querySelector('#domainId').value.trim(),
            domainClass: document.querySelector('#domainClass').value.trim(),
            domainName: document.querySelector('#domainName').value.trim(),
            rawQuery: document.querySelector('#rawQuery').value,
            orderBy: document.querySelector('#orderBy').value.trim(),
            includeCreateUpdate: document.querySelector('#includeCreateUpdate').checked,
            includeExcel: document.querySelector('#includeExcel').checked,
            includePrivacy: document.querySelector('#includePrivacy').checked
        };

        try {
            const response = await axios.post('/system/scaffold/generate', request);
            // common-utils 전역 인터셉터가 ApiResponse를 언래핑하므로 response.data가 곧 산출물 맵이다
            const body = response.data;

            // 세션 만료 시 로그인 페이지 HTML이 돌아온다 (ApiResponse가 아니라 언래핑되지 않음)
            if (typeof body === 'string' || !body) {
                const finalUrl = response.request?.responseURL || '';
                console.error('[scaffold] 비정상 응답 — 최종 URL:', finalUrl);
                CommonUtils.toast(finalUrl.includes('/login')
                    ? '세션이 없어 로그인 페이지로 이동되었습니다. 다시 로그인하세요.'
                    : '응답이 올바르지 않습니다. F12 Console을 확인하세요.', 'error');
                return;
            }

            results = body;
            renderTabs();
            CommonUtils.toast('생성되었습니다. 코드를 검토 후 반영하세요.', 'success');
        } catch (error) {
            // 오류 알림은 common-utils 전역 인터셉터가 모달로 표시한다. 여기서는 기록만 남긴다.
            console.error('[scaffold] 생성 실패', error);
        }
    });

    function renderTabs() {
        const tabContainer = document.querySelector('#result-tabs');
        tabContainer.innerHTML = '';
        Object.keys(results).forEach((key, index) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn btn-sm ' + (index === 0 ? 'btn-dark' : 'btn-outline-dark');
            button.textContent = key;
            button.addEventListener('click', () => selectTab(key, button));
            tabContainer.appendChild(button);
        });
        document.querySelector('#result-card').style.display = '';
        const firstKey = Object.keys(results)[0];
        if (firstKey) {
            selectTab(firstKey, tabContainer.querySelector('button'));
        }
    }

    function selectTab(key, button) {
        currentKey = key;
        document.querySelectorAll('#result-tabs button').forEach(b => {
            b.className = 'btn btn-sm btn-outline-dark';
        });
        button.className = 'btn btn-sm btn-dark';
        document.querySelector('#result-content').textContent = results[key];
    }

    document.querySelector('#btn-copy').addEventListener('click', async () => {
        if (!currentKey) {
            return;
        }
        await navigator.clipboard.writeText(results[currentKey]);
        CommonUtils.toast(currentKey + ' 내용을 복사했습니다.', 'info');
    });
});
