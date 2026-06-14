package com.police.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.ai.entity.AiConversation;
import com.police.ai.mapper.AiConversationMapper;
import com.police.ai.service.AiAnalysisService;
import com.police.ai.service.AiAsrService;
import com.police.ai.service.AiEquipmentService;
import com.police.ai.service.AiOmniService;
import com.police.ai.service.AiScheduleService;
import com.police.ai.service.AiTtsService;
import com.police.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI 能力控制器
 *
 * <p>提供智能排班推荐、装备推荐、警情研判、通用对话及历史查询。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiScheduleService aiScheduleService;
    private final AiEquipmentService aiEquipmentService;
    private final AiAnalysisService aiAnalysisService;
    private final AiOmniService aiOmniService;
    private final AiAsrService aiAsrService;
    private final AiTtsService aiTtsService;
    private final AiConversationMapper conversationMapper;

    /** SSE 流式：智能排班推荐 */
    @PostMapping(value = "/schedule/recommend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter scheduleRecommend(@RequestBody Map<String, Object> req) {
        SseEmitter emitter = new SseEmitter(60_000L);
        String taskType  = (String) req.getOrDefault("taskType", "例行巡逻");
        String areaName  = (String) req.getOrDefault("areaName", "");
        String taskStart = (String) req.getOrDefault("taskStart", "");
        String taskEnd   = (String) req.getOrDefault("taskEnd", "");
        Object taskIdObj = req.get("taskId");

        CompletableFuture.runAsync(() -> {
            try {
                if (taskIdObj != null && !"".equals(taskIdObj.toString()) && !"null".equals(taskIdObj.toString())) {
                    aiScheduleService.recommend(Long.valueOf(taskIdObj.toString()),
                            t -> safeSend(emitter, t), emitter::complete);
                } else {
                    aiScheduleService.recommend(taskType, areaName, taskStart, taskEnd,
                            t -> safeSend(emitter, t), emitter::complete);
                }
            } catch (Exception e) {
                log.error("排班推荐 SSE 异常", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** JSON：装备推荐 */
    @PostMapping("/equipment/recommend")
    @PreAuthorize("hasAuthority('patrol:view')")    public Result<String> equipmentRecommend(@RequestBody Map<String, Object> req) {
        try {
            String taskType     = (String) req.getOrDefault("taskType", "");
            String shiftType    = (String) req.getOrDefault("shiftType", "");
            String areaName     = (String) req.getOrDefault("areaName", "");
            String topCaseTypes = (String) req.getOrDefault("topCaseTypes", "");
            String answer = aiEquipmentService.recommend(taskType, shiftType, areaName, topCaseTypes);
            return Result.ok(answer);
        } catch (Exception e) {
            log.error("装备推荐失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    /** SSE 流式：通用 AI 对话（带实时数据上下文） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, Object> req) {
        SseEmitter emitter = new SseEmitter(60_000L);
        String userMessage = (String) req.getOrDefault("message", "");
        CompletableFuture.runAsync(() -> {
            try {
                aiAnalysisService.generalChat(userMessage,
                        t -> safeSend(emitter, t), emitter::complete);
            } catch (Exception e) {
                log.error("AI 对话 SSE 异常", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** SSE 流式：警情研判分析 */
    @PostMapping(value = "/analysis/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analysisChat(@RequestBody Map<String, Object> req) {
        SseEmitter emitter = new SseEmitter(120_000L);
        String focus = (String) req.getOrDefault("focus", "");
        CompletableFuture.runAsync(() -> {
            try {
                aiAnalysisService.analyzeRecent(focus,
                        t -> safeSend(emitter, t), emitter::complete);
            } catch (Exception e) {
                log.error("警情研判 SSE 异常", e);
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /** 对话历史 */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('sys:user:view')")    public Result<IPage<AiConversation>> history(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.ok(conversationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<AiConversation>()
                        .orderByDesc(AiConversation::getCreatedAt)));
    }

    // ===== 阶段四：多模态 =====

    /** 图片 AI 描述（上传证据后自动标注） */
    @PostMapping("/omni/describe-image")
    @PreAuthorize("hasAuthority('case:edit')")
    public Result<String> describeImage(@RequestBody Map<String, String> req) {
        try {
            String image = req.get("image");
            if (image == null || image.isBlank()) return Result.fail(400, "图片数据为空");
            return Result.ok(aiOmniService.describeImage(image));
        } catch (Exception e) {
            log.error("图片描述失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    /** 视频 AI 摘要（URL 方式） */
    @PostMapping("/omni/summarize-video")
    @PreAuthorize("hasAuthority('patrol:view')")    public Result<String> summarizeVideo(@RequestBody Map<String, String> req) {
        try {
            String videoUrl = req.get("videoUrl");
            if (videoUrl == null || videoUrl.isBlank()) return Result.fail(400, "视频 URL 为空");
            return Result.ok(aiOmniService.summarizeVideo(videoUrl));
        } catch (Exception e) {
            log.error("视频摘要失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    /** 视频分析（base64 上传方式，支持 mp4/mov/avi/webm） */
    @PostMapping("/omni/analyze-video")
    @PreAuthorize("hasAuthority('patrol:view')")    public Result<String> analyzeVideo(@RequestBody Map<String, String> req) {
        try {
            String video = req.get("video");
            String mime = req.getOrDefault("mimeType", "video/mp4");
            if (video == null || video.isBlank()) return Result.fail(400, "视频数据为空");
            String result = aiOmniService.analyzeVideoBase64(video, mime);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("视频分析失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    // ===== 阶段五：语音 =====

    /** ASR 语音识别 */
    @PostMapping("/asr/transcribe")
    @PreAuthorize("hasAuthority('alarm:add')")
    public Result<String> transcribe(@RequestBody Map<String, String> req) {
        try {
            String audio = req.get("audio");
            String format = req.getOrDefault("format", "wav");
            return Result.ok(aiAsrService.transcribe(audio, format));
        } catch (Exception e) {
            log.error("语音识别失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    /** ASR + 结构化提取（语音 → 警情表单字段） */
    @PostMapping("/asr/extract")
    @PreAuthorize("hasAuthority('alarm:add')")
    public Result<String> extractAlarm(@RequestBody Map<String, String> req) {
        try {
            String audio = req.get("audio");
            String format = req.getOrDefault("format", "wav");
            return Result.ok(aiAsrService.transcribeAndExtract(audio, format));
        } catch (Exception e) {
            log.error("语音提取失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    /** TTS 文字转语音 */
    @PostMapping("/tts/speak")
    @PreAuthorize("hasAuthority('sys:user:view')")    public Result<String> speak(@RequestBody Map<String, String> req) {
        try {
            String text = req.get("text");
            String voice = req.getOrDefault("voice", "苏打");
            String emotion = req.getOrDefault("emotion", "normal");
            return Result.ok(aiTtsService.speak(text, voice, emotion));
        } catch (Exception e) {
            log.error("语音合成失败", e);
            return Result.fail("AI 服务暂不可用");
        }
    }

    private void safeSend(SseEmitter emitter, String token) {
        try {
            emitter.send(SseEmitter.event().data(token));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
