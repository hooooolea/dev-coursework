package com.police.common.util;

import com.police.system.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 上下文工具类
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户
     */
    public static SysUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof SysUser) {
            return (SysUser) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户 ID
     */
    public static Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user == null ? null : user.getId();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        SysUser user = getCurrentUser();
        return user == null ? null : user.getUsername();
    }

    /**
     * 获取当前登录用户真实姓名
     */
    public static String getCurrentRealName() {
        SysUser user = getCurrentUser();
        return user == null ? null : user.getRealName();
    }
}
