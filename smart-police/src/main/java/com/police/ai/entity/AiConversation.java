package com.police.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话历史 实体
 *
 * <p>记录 AI 各场景（排班推荐、装备推荐、警情研判、通用对话、语音识别、语音合成、多模态等）
 * 的请求上下文和响应结果，用于查询审计和效果评估。</p>
 */
@Data
@TableName("ai_conversation")
public class AiConversation {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 场景：schedule / equipment / analysis / chat / asr / tts / omni */
    private String scene;

    /** 实际调用的模型 ID */
    private String modelUsed;

    /** 操作用户 ID */
    private Long userId;

    /** 输入 token 数 */
    private Integer inputTokens;

    /** 输出 token 数 */
    private Integer outputTokens;

    /** 上下文摘要 */
    private String promptSummary;

    /** AI 响应文本 */
    private String responseText;

    /** 响应延迟（毫秒） */
    private Integer latencyMs;

    /** 是否采纳：0 未采纳，1 已采纳 */
    private Integer isAdopted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
