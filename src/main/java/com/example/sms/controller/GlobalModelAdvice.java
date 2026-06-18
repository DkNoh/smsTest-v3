package com.example.sms.controller;

import com.example.sms.auth.SmsUserPrincipal;
import com.example.sms.service.menu.MenuSource;
import com.example.sms.service.menu.PageAuth;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    public GlobalModelAdvice(MenuSource menuSource, Environment environment) {
        this.menuSource = menuSource;
        this.environment = environment;
    }

    @ModelAttribute
    public void addLayoutAttributes(@AuthenticationPrincipal SmsUserPrincipal principal,
                                    Model model,
                                    HttpServletRequest request) {
        if (principal == null) {
            model.addAttribute("pageAuth", PageAuth.none());
            return;
        }
        model.addAttribute("user", principal);
        model.addAttribute("menus", menuSource.getMenuTree(principal.getRoleCodes()));
        model.addAttribute("pageAuth", resolvePageAuth(principal, request));
    }

    private PageAuth resolvePageAuth(SmsUserPrincipal principal, HttpServletRequest request) {
        if (isLocalProfile()) {
            return PageAuth.all();
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return PageAuth.from(menuSource.getPermissions(normalizePath(path), principal.getRoleCodes()));
    }

    private boolean isLocalProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("local"::equals);
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
