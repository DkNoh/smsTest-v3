package com.example.sms.service.menu;

import java.util.List;
import java.util.Set;

/**
 * 메뉴 URL과 역할 목록으로 보유 권한을 조회한다.
 * 메뉴가 없거나 부여된 권한이 없으면 빈 Set을 반환한다.
 */
public interface MenuAuthProvider {

    Set<MenuPermission> getPermissions(String menuUrl, List<String> roleCodes);
}
