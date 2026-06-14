package com.police.ai.service;

import com.police.ai.client.MiMoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * AI 语音合成服务（mimo-v2.5-tts）
 *
 * <p>将文字转为 WAV 音频 base64，前端直接播放。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTtsService {

    private final MiMoClient miMoClient;

    /**
     * 文字 → 语音 base64
     * @param text 要播报的文字
     * @param voice 音色（默认"苏打"）
     * @param emotion 语气（normal/urgent/calm）
     * @return WAV base64
     */
    public String speak(String text, String voice, String emotion) throws IOException {
        String body = String.format("""
                {
                    "model": "mimo-v2.5-tts",
                    "messages": [
                        {"role": "user", "content": "播报风格：%s，语速适中，清晰有力"},
                        {"role": "assistant", "content": "%s"}
                    ],
                    "audio": {"format": "wav", "voice": "%s"}
                }
                """, emotion != null ? emotion : "normal", text, voice != null ? voice : "苏打");
        return miMoClient.chatRaw("mimo-v2.5-tts", body);
    }

    /** 紧急警情播报 */
    public String emergencyAlarm(String alarmNo, String location, String alarmType) throws IOException {
        String text = String.format("紧急警情！编号%s，地点：%s，类型：%s。请相关警员立即响应！", alarmNo, location, alarmType);
        return speak(text, "苏打", "urgent");
    }

    /** 调度通知播报 */
    public String dispatchNotify(String officerName, String taskName, String area) throws IOException {
        String text = String.format("%s同志，您有一个新的调度任务：%s，地点：%s，请及时处理。", officerName, taskName, area);
        return speak(text, "苏打", "normal");
    }
}
