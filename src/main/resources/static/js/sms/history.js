document.addEventListener('DOMContentLoaded', function () {
    const HistoryDisplay = (() => {
        const styleId = 'sms-history-chip-style';
        const toneClass = {
            primary: 'history-chip-primary',
            info: 'history-chip-info',
            success: 'history-chip-success',
            warning: 'history-chip-warning',
            danger: 'history-chip-danger',
            muted: 'history-chip-muted'
        };

        const escapeHtml = (value) => String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');

        const injectStyle = () => {
            if (document.getElementById(styleId)) {
                return;
            }
            const style = document.createElement('style');
            style.id = styleId;
            style.textContent = `
                .history-chip {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    min-height: 26px;
                    padding: 5px 10px;
                    border-radius: 999px;
                    font-size: 12px;
                    font-weight: 800;
                    line-height: 1;
                    white-space: nowrap;
                    vertical-align: middle;
                }
                .history-chip-primary { color: #1d4ed8; background: #dbeafe; }
                .history-chip-info { color: #155e75; background: #cffafe; }
                .history-chip-success { color: #166534; background: #dcfce7; }
                .history-chip-warning { color: #92400e; background: #fef3c7; }
                .history-chip-danger { color: #b91c1c; background: #fee2e2; }
                .history-chip-muted { color: #334155; background: #f1f5f9; }
            `;
            document.head.appendChild(style);
        };

        const chip = ({ text, tone = 'muted' }) => {
            const cls = toneClass[tone] || toneClass.muted;
            return `<span class="history-chip ${cls}">${escapeHtml(text || '-')}</span>`;
        };

        const mappedChip = (map) => ({ value }) => {
            const option = map[value] || { text: value || '-', tone: 'muted' };
            return chip(option);
        };

        return { injectStyle, mappedChip };
    })();

    HistoryDisplay.injectStyle();

    const sendTypeFormatter = HistoryDisplay.mappedChip({
        SMS: { text: 'SMS', tone: 'primary' },
        LMS: { text: 'LMS', tone: 'info' },
        MMS: { text: 'MMS', tone: 'warning' },
        ALIMTALK: { text: '알림톡', tone: 'success' }
    });

    const sendStatusFormatter = HistoryDisplay.mappedChip({
        READY: { text: '대기', tone: 'muted' },
        SENT: { text: '발송', tone: 'primary' },
        SUCCESS: { text: '성공', tone: 'success' },
        FAIL: { text: '실패', tone: 'danger' },
        CANCEL: { text: '취소', tone: 'warning' }
    });

    const resultCodeFormatter = HistoryDisplay.mappedChip({
        C000: { text: '정상', tone: 'success' },
        C001: { text: '취소', tone: 'warning' },
        E001: { text: '오류', tone: 'danger' }
    });

    const pageBuilder = new TuiPageBuilder({
        el: 'grid',
        apiUrl: '/sms/history/data',
        searchInputs: ['sendType', 'sendStatus', 'sentAt', 'receiverNo'],
        searchDefaults: {},
        rowHeaders: ['rowNum'],
        columns: [
            { header: 'SMS_HISTORY_ID', name: 'smsHistoryId', align: 'center', width: 150 },
            { header: 'REQUEST_ID', name: 'requestId', align: 'center', width: 150 },
            { header: 'SENT_AT', name: 'sentAt', align: 'center', width: 150, editable: true, formatter: TuiCommon.fmt.date },
            { header: 'RECEIVER_NO', name: 'receiverNo', align: 'center', width: 150, editable: true },
            { header: 'SENDER_NO', name: 'senderNo', align: 'center', width: 150, editable: true },
            { header: 'SEND_TYPE', name: 'sendType', align: 'center', width: 130, editable: true, formatter: sendTypeFormatter },
            { header: 'SEND_STATUS', name: 'sendStatus', align: 'center', width: 130, editable: true, formatter: sendStatusFormatter },
            { header: 'RESULT_CD', name: 'resultCd', align: 'center', width: 120, editable: true, formatter: resultCodeFormatter },
            { header: 'RESULT_MSG', name: 'resultMsg', align: 'center', width: 150, editable: true },
            { header: 'UPD_DTTM', name: 'updDttm', hidden: true, modalVisible: false }
        ],
        autoModal: true,
        autoModalTitle: '발송이력조회 상세',
        modalActions: {
            createUrl: '/sms/history/create',
            updateUrl: '/sms/history/update',
            deleteUrl: '/sms/history/delete',
            pkField: 'smsHistoryId',
            lockField: 'updDttm',
            beforeLockField: 'beforeUpdDttm',
            editable: true
        }
    });
});
