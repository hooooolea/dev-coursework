package com.police.ai.service;

import com.police.ai.client.MiMoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * AI 多模态服务 — 图片描述 / 视频摘要（mimo-v2.5-omni）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiOmniService {

    private final MiMoClient miMoClient;

    /** 对 base64 图片生成描述（用于证据上传后自动标注） */
    public String describeImage(String base64Image) throws IOException {
        // 构建多模态消息（Omni 模型支持图片输入）
        String body = String.format("""
                {
                    "model": "mimo-v2.5-omni",
                    "messages": [{
                        "role": "user",
                        "content": [
                            {"type": "text", "text": "这是警方取证的现场图片，请用100字以内客观描述图中内容，注意记录关键物证特征。"},
                            {"type": "image_url", "image_url": {"url": "data:image/jpeg;base64,%s"}}
                        ]
                    }],
                    "max_tokens": 512,
                    "temperature": 0.3
                }
                """, base64Image);
        return miMoClient.chatRaw("mimo-v2.5-omni", body);
    }

    /** 对视频 URL 生成摘要 */
    public String summarizeVideo(String videoUrl) throws IOException {
        String body = String.format("""
                {
                    "model": "mimo-v2.5-omni",
                    "messages": [{
                        "role": "user",
                        "content": [
                            {"type": "text", "text": "这是巡逻执法记录仪拍摄的视频，请用150字以内总结视频中的关键事件和执法过程。"},
                            {"type": "video_url", "video_url": {"url": "%s"}}
                        ]
                    }],
                    "max_tokens": 512,
                    "temperature": 0.3
                }
                """, videoUrl);
        return miMoClient.chatRaw("mimo-v2.5-omni", body);
    }

    /** 对 base64 视频进行分析 */
    public String analyzeVideoBase64(String base64Video, String mimeType) throws IOException {
        String body = String.format("""
                {
                    "model": "mimo-v2.5-omni",
                    "messages": [{
                        "role": "user",
                        "content": [
                            {"type": "text", "text": "请分析这段警务视频：1.描述视频中的关键场景和事件 2.识别可疑行为或异常情况 3.如有人员或车辆，描述其特征。请用200字以内完成分析。"},
                            {"type": "video_url", "video_url": {"url": "data:%s;base64,%s"}}
                        ]
                    }],
                    "max_tokens": 1024,
                    "temperature": 0.3
                }
                """, mimeType != null ? mimeType : "video/mp4", base64Video);
        return miMoClient.chatRaw("mimo-v2.5-omni", body);
    }
}
