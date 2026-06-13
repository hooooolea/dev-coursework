package com.police.ai.service;

import com.police.ai.client.MiMoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * AI 语音识别服务（mimo-v2.5-asr）
 *
 * <p>将音频 base64（WAV/MP3）转为文字，可进一步用 Pro 模型提取结构化警情字段。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAsrService {

    private final MiMoClient miMoClient;

    /**
     * 语音 → 文字（ASR）
     * @param base64Audio 音频 base64 编码
     * @param format 音频格式（wav / mp3）
     * @return 识别出的文字
     */
    public String transcribe(String base64Audio, String format) throws IOException {
        String body = String.format("""
                {
                    "model": "mimo-v2.5-asr",
                    "messages": [{
                        "role": "user",
                        "content": [
                            {"type": "input_audio", "input_audio": {"data": "%s", "format": "%s"}}
                        ]
                    }],
                    "max_tokens": 1024
                }
                """, base64Audio, format != null ? format : "wav");
        return miMoClient.chatRaw("mimo-v2.5-asr", body);
    }

    /**
     * 语音 → 结构化警情（ASR + Pro 提取）
     * @return JSON 格式的 {caller, address, description, urgency}
     */
    public String transcribeAndExtract(String base64Audio, String format) throws IOException {
        String text = transcribe(base64Audio, format);
        String prompt = "从以下报警录音文字中提取结构化信息，以 JSON 格式返回：{\"caller\":\"报警人\",\"address\":\"事发地址\",\"description\":\"事件描述\",\"urgency\":\"一般/较紧急/紧急/特急\"}。\n原文：" + text;
        return miMoClient.chatPro("你是警务报警信息提取助手。", prompt);
    }
}
