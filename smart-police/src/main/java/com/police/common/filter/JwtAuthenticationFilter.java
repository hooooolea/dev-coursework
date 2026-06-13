package com.police.common.filter;

import com.police.common.constant.CacheKey;
import com.police.common.util.JwtUtil;
import com.police.system.entity.SysUser;
import com.police.system.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 — 每次请求验证 Token，注入 SecurityContext
 * 用 @Lazy 注入 UserService，打破与 SecurityConfig 的循环依赖
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   RedisTemplate<String, Object> redisTemplate,
                                   @Lazy UserService userService) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            Boolean inBlacklist = (Boolean) redisTemplate.opsForValue()
                    .get(CacheKey.BLACKLIST_PREFIX + token);
            if (Boolean.TRUE.equals(inBlacklist)) {
                log.debug("Token 在黑名单中，拒绝请求");
            } else if (jwtUtil.isValid(token)) {
                Long userId = jwtUtil.getUserId(token);
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SysUser user = userService.loadUserWithPerms(jwtUtil.getUsername(token));
                    if (user != null && user.isEnabled() && user.isAccountNonLocked()) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
