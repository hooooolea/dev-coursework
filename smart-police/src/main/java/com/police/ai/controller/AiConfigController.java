package com.police.ai.controller;

import com.police.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 配置 控制器
 *
 * <p>提供 MiMo API Key 的读取与保存接口，API Key 存储在 Redis 中，
 * 支持设置和清除操作，设置时自动刷新 TTL。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/config")
@RequiredArgsConstructor
public class AiConfigController {

    private final StringRedisTemplate redis;

    private static final String REDIS_KEY = "ai:mimo:api-key";

    private static final long TTL_DAYS = 365;

    /**
     * 获取 API Key 的配置状态
     *
     * <p>返回当前是否已配置 API Key，以及脱敏后的 Key 字符串（前3后3，中间掩码）。</p>
     *
     * @return 包含 configured（布尔）和 maskedKey（字符串）的 Map
     */
    @GetMapping("/key")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<Map<String, Object>> getKeyStatus() {
        String key = redis.opsForValue().get(REDIS_KEY);
        boolean configured = key != null && !key.isBlank();
        String masked = null;
        if (configured && key.length() > 6) {
            masked = key.substring(0, 3) + "****" + key.substring(key.length() - 3);
        }
        return Result.ok(Map.of("configured", configured, "maskedKey", masked != null ? masked : ""));
    }

    /**
     * 保存（或清除）API Key
     *
     * <p>请求体包含 {@code apiKey} 字段时将其保存至 Redis（过期时间 365 天）；
     * 若字段为空或缺失则删除 Redis 中的 Key。</p>
     *
     * @param body 请求体 Map，包含可选的 apiKey 字段
     * @return 操作结果消息
     */
    @PostMapping("/key")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public Result<?> saveKey(@RequestBody Map<String, String> body) {
        String key = body.get("apiKey");
        if (key == null || key.isBlank()) {
            redis.delete(REDIS_KEY);
            log.info("MiMo API Key cleared");
            return Result.ok("已清除");
        }
        redis.opsForValue().set(REDIS_KEY, key.trim(), TTL_DAYS, TimeUnit.DAYS);
        log.info("MiMo API Key updated");
        return Result.ok("已保存");
    }
}
