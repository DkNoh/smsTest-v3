document.addEventListener('DOMContentLoaded', function () {
    const pageBuilder = new TuiPageBuilder({
        el: 'grid',
        apiUrl: '/sms/history/data',
        searchInputs: ['startDt'],
        rowHeaders: ['rowNum'],
        columns: [
            { header: 'SENT_AT', name: 'sentAt', align: 'center', width: 150, formatter: TuiCommon.fmt.date },
            { header: 'RECEIVER_NO', name: 'receiverNo', align: 'center', width: 150 },
            { header: 'SEND_TYPE', name: 'sendType', align: 'center', width: 150 }
        ]
    });

    document.querySelector('#btn-excel')?.addEventListener('click', () => {
        const qs = '?' + [`startDt=${document.querySelector('#startDt').value}`].join('&');
        window.location.href = '/sms/history/excel' + qs;
    });
});
