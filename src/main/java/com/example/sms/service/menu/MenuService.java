package com.example.sms.service.menu;

import com.example.sms.vo.menu.MenuItemVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final MenuProvider menuProvider;

    public MenuService(MenuProvider menuProvider) {
        this.menuProvider = menuProvider;
    }

    public List<MenuItemVO> getMenuTree(List<String> roleCodes) {
        return menuProvider.getMenuTree(roleCodes);
    }
}