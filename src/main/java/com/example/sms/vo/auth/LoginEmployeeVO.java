package com.example.sms.vo.auth;

public class LoginEmployeeVO {

    private String empId;
    private String depId;
    private String empNm;
    private String depNm;
    private String actYn;
    private String depActYn;

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

    public String getEmpNm() {
        return empNm;
    }

    public void setEmpNm(String empNm) {
        this.empNm = empNm;
    }

    public String getDepNm() {
        return depNm;
    }

    public void setDepNm(String depNm) {
        this.depNm = depNm;
    }

    public String getActYn() {
        return actYn;
    }

    public void setActYn(String actYn) {
        this.actYn = actYn;
    }

    public String getDepActYn() {
        return depActYn;
    }

    public void setDepActYn(String depActYn) {
        this.depActYn = depActYn;
    }
}
