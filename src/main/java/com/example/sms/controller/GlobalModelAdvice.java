package com.example.sms.controller;

import com.example.sms.auth.SmsUserPrincipal;
import com.example.sms.service.menu.MenuSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * defaultLayout(sidebar/header)이 모든 화면에서 사용하는 공통 모델을 채운다.
 * 메뉴 렌더링은 Controller가 넘긴 메뉴 tree만 사용한다는 규칙의 단일 적용 지점이다.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final MenuSource menuSource;

    public GlobalModelAdvice(MenuSource menuSource) {
        this.menuSource = menuSource;
    }

    @ModelAttribute
    public void addLayoutAttributes(@AuthenticationPrincipal SmsUserPrincipal principal, Model model) {
        if (principal == null) {
            return;
        }
        model.addAttribute("user", principal);
        model.addAttribute("menus", menuSource.getMenuTree(principal.getRoleCodes()));
    }
}
