package com.example.sms.service.menu;

import com.example.sms.mapper.menu.MenuAuthMapper;
import com.example.sms.vo.menu.MenuAuthVO;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "sms.menu.source", havingValue = "db")
public class DbMenuAuthProvider implements MenuAuthProvider {

    private static final String YES = "Y";

    private final MenuAuthMapper menuAuthMapper;

    public DbMenuAuthProvider(MenuAuthMapper menuAuthMapper) {
        this.menuAuthMapper = menuAuthMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<MenuPermission> getPermissions(String menuUrl, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new IllegalArgumentException("roleCodes must not be empty for db menu source");
        }
        MenuAuthVO auth = menuAuthMapper.selectMenuPermissions(menuUrl, roleCodes);
        if (auth == null) {
            return EnumSet.noneOf(MenuPermission.class);
        }

        Set<MenuPermission> permissions = EnumSet.noneOf(MenuPermission.class);
        addIfGranted(permissions, MenuPermission.READ, auth.getCanRead());
        addIfGranted(permissions, MenuPermission.CREATE, auth.getCanCreate());
        addIfGranted(permissions, MenuPermission.UPDATE, auth.getCanUpdate());
        addIfGranted(permissions, MenuPermission.DELETE, auth.getCanDelete());
        addIfGranted(permissions, MenuPermission.APPROVE, auth.getCanApprove());
        addIfGranted(permissions, MenuPermission.CANCEL, auth.getCanCancel());
        addIfGranted(permissions, MenuPermission.DOWNLOAD, auth.getCanDownload());
        addIfGranted(permissions, MenuPermission.MASK_VIEW, auth.getCanMaskView());
        return permissions;
    }

    private void addIfGranted(Set<MenuPermission> permissions, MenuPermission permission, String flag) {
        if (YES.equals(flag)) {
            permissions.add(permission);
        }
    }
}
