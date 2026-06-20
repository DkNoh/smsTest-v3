package com.example.sms.dto.system;

public class ScaffoldMenuOptionDTO {

    private String menuId;
    private String parentMenuId;
    private String roleCode;
    private Integer sortOrd;

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Integer getSortOrd() {
        return sortOrd;
    }

    public void setSortOrd(Integer sortOrd) {
        this.sortOrd = sortOrd;
    }
}
