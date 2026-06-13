package com.police.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.police.ai.config.MiMoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * MiMo AI API 客户端
 *
 * <p>基于 OkHttp 的 HTTP 客户端，调用 MiMo API（兼容 OpenAI 接口格式）。
 * 支持 SSE（Server-Sent Events）流式响应和普通 JSON 响应两种模式。</p>
 *
 * <p>认证方式为 {@code api-key} 请求头（非 {@code Authorization: Bearer}）。</p>
 */
@Slf4j
@Component
public class MiMoClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String DATA_PREFIX = "data: ";

    private static final String DONE_MARKER = "[DONE]";

    private final MiMoConfig config;

    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient;

    private final StringRedisTemplate redis;

    /**
     * 构造 MiMoClient
     *
     * <p>根据配置创建共享 OkHttpClient 实例，连接超时和读取超时均从配置读取。</p>
     *
     * @param config       MiMo 配置（baseUrl、apiKey、超时等）
     * @param objectMapper Jackson ObjectMapper（用于请求序列化与响应反序列化）
     */
    public MiMoClient(MiMoConfig config, ObjectMapper objectMapper, StringRedisTemplate redis) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.redis = redis;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 构建聊天请求体
     *
     * @param model        模型 ID
     * @param systemPrompt 系统提示词（可 null 或空白）
     * @param userMessage  用户消息
     * @param stream       是否启用 SSE 流式响应
     * @param maxTokens    最大 token 数（可 null）
     * @param temperature  温度参数（可 null）
     * @return 请求体 Map
     */
    private Map<String, Object> buildChatBody(String model, String systemPrompt, String userMessage,
                                               boolean stream, Integer maxTokens, Double temperature) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);

        body.put("stream", stream);
        if (maxTokens != null) {
            body.put("max_tokens", maxTokens);
        }
        if (temperature != null) {
            body.put("temperature", temperature);
        }
        return body;
    }

    /**
     * 获取 MiMo API Key
     *
     * <p>优先从 Redis（{@code ai:mimo:api-key}）读取，若 Redis 中不存在或为空
     * 则回退到配置文件中的 {@code ai.mimo.api-key}。</p>
     *
     * @return API Key 字符串
     */
    private String getApiKey() {
        String key = redis.opsForValue().get("ai:mimo:api-key");
        if (key != null && !key.isBlank()) {
            return key;
        }
        return config.getApiKey();
    }

    /**
     * 构建 OkHttp POST 请求
     *
     * <p>组装完整 URL、设置 {@code api-key} 认证头及 JSON Content-Type。</p>
     *
     * @param body 请求体对象（将被序列化为 JSON）
     * @return OkHttp Request
     * @throws IOException JSON 序列化失败时抛出
     */
    private Request buildPostRequest(Object body) throws IOException {
        String json = objectMapper.writeValueAsString(body);
        return new Request.Builder()
                .url(config.getBaseUrl() + CHAT_COMPLETIONS_PATH)
                .header("api-key", getApiKey())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();
    }

    /**
     * 调用 MiMo 聊天 API（SSE 流式）
     *
     * <p>通过 Server-Sent Events 实时返回每个 token，适用于需要逐字展示
     * 模型输出的场景（如流式对话、打字机效果）。API 返回 {@code [DONE]}
     * 标记时代表流结束，此时触发 onComplete 回调。</p>
     *
     * @param model        模型 ID（如 mimo-v2.5-pro）
     * @param systemPrompt 系统提示词（可 null）
     * @param userMessage  用户消息内容
     * @param onToken      token 回调，每收到一个 token 块时触发
     * @param onComplete   流结束回调
     * @throws IOException 网络或 IO 异常
     */
    public void streamChat(String model, String systemPrompt, String userMessage,
                           Consumer<String> onToken, Runnable onComplete) throws IOException {
        Map<String, Object> body = buildChatBody(model, systemPrompt, userMessage, true, null, null);
        Request request = buildPostRequest(body);

        log.debug("MiMo SSE request: model={}, stream=true", model);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "unknown";
                log.error("MiMo API returned error status: {}, body={}", response.code(), errorBody);
                throw new IOException("MiMo API returned " + response.code() + ": " + errorBody);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("MiMo API returned empty body");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (!line.startsWith(DATA_PREFIX)) {
                        log.trace("Skipping non-data SSE line: {}", line);
                        continue;
                    }

                    String data = line.substring(DATA_PREFIX.length()).trim();
                    if (DONE_MARKER.equals(data)) {
                        log.debug("MiMo SSE stream completed");
                        onComplete.run();
                        return;
                    }

                    try {
                        JsonNode jsonNode = objectMapper.readTree(data);
                        JsonNode contentNode = jsonNode.path("choices")
                                .path(0)
                                .path("delta")
                                .path("content");
                        if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                            String token = contentNode.asText();
                            if (!token.isEmpty()) {
                                onToken.accept(token);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse SSE data chunk: {}", data, e);
                    }
                }
            }
        }
    }

    /**
     * 调用 MiMo 聊天 API（普通 JSON 响应）
     *
     * <p>发送请求并等待完整响应，返回模型生成的文本内容。
     * 适用于无需实时展示流式输出的场景（如后台分析、批量处理）。</p>
     *
     * @param model        模型 ID（如 mimo-v2.5-pro）
     * @param systemPrompt 系统提示词（可 null）
     * @param userMessage  用户消息内容
     * @return 模型回复文本
     * @throws IOException 网络或 IO 异常，或 API 返回错误时抛出
     */
    public String chatJson(String model, String systemPrompt, String userMessage) throws IOException {
        Map<String, Object> body = buildChatBody(model, systemPrompt, userMessage, false, null, null);
        Request request = buildPostRequest(body);

        log.debug("MiMo JSON request: model={}, stream=false", model);

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "unknown";
                log.error("MiMo API returned error status: {}, body={}", response.code(), errorBody);
                throw new IOException("MiMo API returned " + response.code() + ": " + errorBody);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("MiMo API returned empty body");
            }

            String jsonResponse = responseBody.string();
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 检查 API 返回的业务错误
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode() && !errorNode.isNull()) {
                String errorMsg = errorNode.path("message").asText("Unknown error");
                log.error("MiMo API response error: {}", errorMsg);
                throw new IOException("MiMo API error: " + errorMsg);
            }

            JsonNode contentNode = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                log.warn("MiMo response missing content field, full response: {}", jsonResponse);
                return "";
            }

            return contentNode.asText();
        }
    }

    /**
     * 使用默认高级模型进行 SSE 流式聊天
     *
     * <p>便捷方法，自动使用 {@link MiMoConfig#getModelPro()} 配置的模型 ID，
     * 无需每次调用时手动传入模型参数。</p>
     *
     * @param systemPrompt 系统提示词（可 null）
     * @param userMessage  用户消息内容
     * @param onToken      token 回调
     * @param onComplete   流结束回调
     * @throws IOException 网络或 IO 异常
     */
    public void streamChatPro(String systemPrompt, String userMessage,
                              Consumer<String> onToken, Runnable onComplete) throws IOException {
        streamChat(config.getModelPro(), systemPrompt, userMessage, onToken, onComplete);
    }

    /**
     * 使用默认高级模型进行普通聊天
     *
     * <p>便捷方法，自动使用 {@link MiMoConfig#getModelPro()} 配置的模型 ID，
     * 无需每次调用时手动传入模型参数。</p>
     *
     * @param systemPrompt 系统提示词（可 null）
     * @param userMessage  用户消息内容
     * @return 模型回复文本
     * @throws IOException 网络或 IO 异常
     */
    /**
     * 使用原始 JSON 字符串直接调用 API（用于非标准请求体，如多模态图片/视频）。
     *
     * @param model    模型 ID
     * @param jsonBody 原始 JSON 请求体字符串
     * @return 模型回复文本
     * @throws IOException 网络或 IO 异常
     */
    public String chatRaw(String model, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(config.getBaseUrl() + CHAT_COMPLETIONS_PATH)
                .header("api-key", getApiKey())
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                throw new IOException("MiMo API " + response.code() + ": " + err);
            }
            ResponseBody rb = response.body();
            if (rb == null) throw new IOException("empty body");
            JsonNode root = objectMapper.readTree(rb.string());
            JsonNode errNode = root.path("error");
            if (!errNode.isMissingNode() && !errNode.isNull()) {
                throw new IOException("MiMo error: " + errNode.path("message").asText());
            }
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isMissingNode() ? "" : content.asText();
        }
    }

    public String chatPro(String systemPrompt, String userMessage) throws IOException {
        return chatJson(config.getModelPro(), systemPrompt, userMessage);
    }
}
