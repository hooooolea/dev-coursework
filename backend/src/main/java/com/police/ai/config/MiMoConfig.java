package com.police.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 小米 MiMo AI 配置
 *
 * <p>配置项前缀 {@code ai.mimo}，通过环境变量 {@code MIMO_API_KEY} 注入 API Key。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.mimo")
public class MiMoConfig {

    /** MiMo API Key（从环境变量 MIMO_API_KEY 读取） */
    private String apiKey;

    /** API 基础地址 */
    private String baseUrl = "https://api.xiaomimimo.com/v1";

    /** 模型 ID：Pro 版本 */
    private String modelPro = "mimo-v2.5-pro";

    /** 模型 ID：Omni 多模态版本 */
    private String modelOmni = "mimo-v2.5-omni";

    /** 模型 ID：ASR 语音识别 */
    private String modelAsr = "mimo-v2.5-asr";

    /** 模型 ID：TTS 语音合成 */
    private String modelTts = "mimo-v2.5-tts";

    /** 生成最大 Token 数 */
    private int maxTokens = 1024;

    /** 生成温度（0 ~ 1，值越小越确定） */
    private double temperature = 0.3;

    /** 连接超时时间（毫秒） */
    private long connectTimeout = 30_000;

    /** 读取超时时间（毫秒） */
    private long readTimeout = 60_000;
}
