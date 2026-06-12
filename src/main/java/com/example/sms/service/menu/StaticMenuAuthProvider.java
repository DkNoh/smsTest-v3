package com.example.sms.service.menu;

import com.example.sms.vo.menu.MenuItemVO;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * static source 전용. 메뉴 테이블 생성 전 local 화면 검증 용도이므로
 * baseline에 존재하는 메뉴 URL에는 모든 권한을 부여한다.
 * 최종 권한 검증은 반드시 db source에서 확인한다.
 */
@Service
@ConditionalOnProperty(name = "sms.menu.source", havingValue = "static")
public class StaticMenuAuthProvider implements MenuAuthProvider {

    private final MenuProvider menuProvider;

    public StaticMenuAuthProvider(MenuProvider menuProvider) {
        this.menuProvider = menuProvider;
    }

    @Override
    public Set<MenuPermission> getPermissions(String menuUrl, List<String> roleCodes) {
        Set<String> menuUrls = new HashSet<>();
        collectUrls(menuProvider.getMenuTree(roleCodes), menuUrls);
        if (menuUrls.contains(menuUrl)) {
            return EnumSet.allOf(MenuPermission.class);
        }
        return EnumSet.noneOf(MenuPermission.class);
    }

    private void collectUrls(List<MenuItemVO> menus, Set<String> menuUrls) {
        for (MenuItemVO menu : menus) {
            if (menu.getMenuUrl() != null) {
                menuUrls.add(menu.getMenuUrl());
            }
            collectUrls(menu.getChildren(), menuUrls);
        }
    }
}
