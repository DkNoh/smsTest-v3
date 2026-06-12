package com.example.sms.service.system.scaffold;

import com.example.sms.dto.system.ScaffoldRequestDTO;
import java.util.List;
import java.util.Map;

/**
 * 템플릿 생성에 필요한 입력 + 분석 결과 묶음.
 */
public class ScaffoldModel {

    private final ScaffoldRequestDTO request;
    private final List<String> columns;
    private final List<String> searchVars;
    private final Map<String, String> typeMap;

    public ScaffoldModel(ScaffoldRequestDTO request, List<String> columns,
                         List<String> searchVars, Map<String, String> typeMap) {
        this.request = request;
        this.columns = List.copyOf(columns);
        this.searchVars = List.copyOf(searchVars);
        this.typeMap = Map.copyOf(typeMap);
    }

    public ScaffoldRequestDTO getRequest() {
        return request;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<String> getSearchVars() {
        return searchVars;
    }

    public Map<String, String> getTypeMap() {
        return typeMap;
    }

    public String moduleName() {
        return request.getModuleName();
    }

    public String domainId() {
        return request.getDomainId();
    }

    public String domainClass() {
        return request.getDomainClass();
    }

    public String domainName() {
        return request.getDomainName();
    }

    public String rawQuery() {
        return request.getRawQuery();
    }

    public String orderBy() {
        return request.getOrderBy();
    }

    public boolean includeCreateUpdate() {
        return request.isIncludeCreateUpdate();
    }

    public boolean includeExcel() {
        return request.isIncludeExcel();
    }

    public boolean includePrivacy() {
        return request.isIncludePrivacy();
    }

    public String screenUrl() {
        return "/" + moduleName() + "/" + domainId();
    }
}
