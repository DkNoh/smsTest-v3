package com.scbk.sms.vo.system;

/**
 * SMS.TB_PRIVACY_AUDIT_LOG 기록용 VO. 행위자는 (EMP_ID, DEP_ID)로 기록한다.
 */
public class PrivacyAuditLogVO {

    private String empId;
    private String depId;
    private String executorIp;
    private String requestUrl;
    private String actionType;
    private String targetData;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public String getExecutorIp() {
        return executorIp;
    }

    public void setExecutorIp(String executorIp) {
        this.executorIp = executorIp;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetData() {
        return targetData;
    }

    public void setTargetData(String targetData) {
        this.targetData = targetData;
    }
}
