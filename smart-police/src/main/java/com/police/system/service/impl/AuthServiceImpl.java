package com.police.system.service.impl;

import com.police.common.constant.CacheKey;
import com.police.common.exception.BusinessException;
import com.police.common.util.JwtUtil;
import com.police.system.dto.LoginDTO;
import com.police.system.dto.LoginVO;
import com.police.system.entity.SysUser;
import com.police.system.service.AuthService;
import com.police.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. Spring Security 认证
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        } catch (AuthenticationException e) {
            // 记录失败次数
            recordLoginFail(dto.getUsername());
            throw BusinessException.of(401, "用户名或密码错误");
        }

        SysUser user = (SysUser) auth.getPrincipal();

        // 2. 生成 Token
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 3. 清除登录失败计数
        redisTemplate.delete(CacheKey.LOGIN_FAIL_PREFIX + dto.getUsername());

        // 4. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginFailCount(0);
        userService.updateById(user);

        return new LoginVO(
                user.getId(), user.getUsername(), user.getRealName(), user.getAvatar(),
                accessToken, refreshToken, user.getRoleCodes(), user.getPermissions());
    }

    @Override
    public void logout(String token) {
        if (token == null) return;
        long expire = jwtUtil.getExpireMillis(token);
        if (expire > 0) {
            redisTemplate.opsForValue().set(
                    CacheKey.BLACKLIST_PREFIX + token, true,
                    expire, TimeUnit.MILLISECONDS);
        }
    }

    private void recordLoginFail(String username) {
        String key = CacheKey.LOGIN_FAIL_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, CacheKey.LOGIN_FAIL_EXPIRE, TimeUnit.SECONDS);
        if (count != null && count >= 5) {
            // 连续失败5次，锁定账号30分钟
            SysUser user = userService.loadUserWithPerms(username);
            if (user != null) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(30));
                user.setLoginFailCount(count.intValue());
                userService.updateById(user);
            }
        }
    }
}
