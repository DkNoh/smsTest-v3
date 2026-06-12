package com.example.sms.dto.system;

import jakarta.validation.constraints.NotBlank;

/**
 * Query Scaffold 생성 요청 (QuerySpec).
 * rawQuery 안의 $변수는 검색조건 규약이다. 예: AND A.SEND_DT >= $start_dt
 */
public class ScaffoldRequestDTO {

    @NotBlank(message = "moduleName은 필수입니다.")
    private String moduleName;

    @NotBlank(message = "domainId는 필수입니다.")
    private String domainId;

    @NotBlank(message = "domainClass는 필수입니다.")
    private String domainClass;

    @NotBlank(message = "domainName은 필수입니다.")
    private String domainName;

    @NotBlank(message = "rawQuery는 필수입니다.")
    private String rawQuery;

    @NotBlank(message = "orderBy는 필수입니다. 결정적 정렬 컬럼을 입력하세요.")
    private String orderBy;

    private boolean includeCreateUpdate;
    private boolean includeExcel;
    private boolean includePrivacy;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getDomainClass() {
        return domainClass;
    }

    public void setDomainClass(String domainClass) {
        this.domainClass = domainClass;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isIncludeCreateUpdate() {
        return includeCreateUpdate;
    }

    public void setIncludeCreateUpdate(boolean includeCreateUpdate) {
        this.includeCreateUpdate = includeCreateUpdate;
    }

    public boolean isIncludeExcel() {
        return includeExcel;
    }

    public void setIncludeExcel(boolean includeExcel) {
        this.includeExcel = includeExcel;
    }

    public boolean isIncludePrivacy() {
        return includePrivacy;
    }

    public void setIncludePrivacy(boolean includePrivacy) {
        this.includePrivacy = includePrivacy;
    }
}
