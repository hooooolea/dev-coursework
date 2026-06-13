package com.police.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire}")
    private long accessExpire;

    @Value("${jwt.refresh-expire}")
    private long refreshExpire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        return buildToken(claims, accessExpire * 1000);
    }

    /**
     * 生成 refreshToken
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        return buildToken(claims, refreshExpire * 1000);
    }

    private String buildToken(Map<String, Object> claims, long expireMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireMillis))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token，失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token 已过期");
        } catch (JwtException e) {
            log.warn("Token 解析失败: {}", e.getMessage());
        }
        return null;
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.get("userId", Long.class);
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.get("username", String.class);
    }

    /**
     * 获取 Token 剩余有效期（毫秒）
     */
    public long getExpireMillis(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return 0;
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean isValid(String token) {
        return parseToken(token) != null;
    }
}
