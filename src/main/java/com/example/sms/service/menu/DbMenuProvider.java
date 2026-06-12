package com.example.sms.service.menu;

import com.example.sms.mapper.menu.MenuMapper;
import com.example.sms.vo.menu.MenuItemVO;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "sms.menu.source", havingValue = "db")
public class DbMenuProvider implements MenuProvider {

    private final MenuMapper menuMapper;
    private final MenuTreeBuilder menuTreeBuilder;

    public DbMenuProvider(MenuMapper menuMapper, MenuTreeBuilder menuTreeBuilder) {
        this.menuMapper = menuMapper;
        this.menuTreeBuilder = menuTreeBuilder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemVO> getMenuTree(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException("roleCodes must not be empty for db menu source");
        }
        List<MenuItemVO> flatMenus = menuMapper.selectReadableMenus(roleCodes);
        return menuTreeBuilder.build(flatMenus);
    }
}