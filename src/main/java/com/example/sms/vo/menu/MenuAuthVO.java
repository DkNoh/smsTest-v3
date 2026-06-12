package com.example.sms.vo.menu;

/**
 * TB_MENU_AUTH 조회 결과. 역할 여러 건은 MAX 집계로 합쳐 'Y' 우선이다.
 */
public class MenuAuthVO {

    private String canRead;
    private String canCreate;
    private String canUpdate;
    private String canDelete;
    private String canApprove;
    private String canCancel;
    private String canDownload;
    private String canMaskView;

    public String getCanRead() {
        return canRead;
    }

    public void setCanRead(String canRead) {
        this.canRead = canRead;
    }

    public String getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(String canCreate) {
        this.canCreate = canCreate;
    }

    public String getCanUpdate() {
        return canUpdate;
    }

    public void setCanUpdate(String canUpdate) {
        this.canUpdate = canUpdate;
    }

    public String getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(String canDelete) {
        this.canDelete = canDelete;
    }

    public String getCanApprove() {
        return canApprove;
    }

    public void setCanApprove(String canApprove) {
        this.canApprove = canApprove;
    }

    public String getCanCancel() {
        return canCancel;
    }

    public void setCanCancel(String canCancel) {
        this.canCancel = canCancel;
    }

    public String getCanDownload() {
        return canDownload;
    }

    public void setCanDownload(String canDownload) {
        this.canDownload = canDownload;
    }

    public String getCanMaskView() {
        return canMaskView;
    }

    public void setCanMaskView(String canMaskView) {
        this.canMaskView = canMaskView;
    }
}
