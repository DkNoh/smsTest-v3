package com.example.sms.service.menu;

import com.example.sms.vo.menu.MenuItemVO;
import java.util.List;

public interface MenuProvider {

    List<MenuItemVO> getMenuTree(List<String> roleCodes);
}