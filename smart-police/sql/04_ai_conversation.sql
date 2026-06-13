-- =====================================================
-- AI 智能板块 建表脚本
-- =====================================================

-- AI 对话历史表
DROP TABLE IF EXISTS ai_conversation;
CREATE TABLE ai_conversation (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    scene          VARCHAR(20)     NOT NULL COMMENT 'schedule/equipment/analysis/chat/asr/tts/omni',
    model_used     VARCHAR(50)     NOT NULL COMMENT '实际调用的模型ID',
    user_id        BIGINT UNSIGNED NOT NULL,
    input_tokens   INT             NOT NULL DEFAULT 0,
    output_tokens  INT             NOT NULL DEFAULT 0,
    prompt_summary VARCHAR(500)             COMMENT '上下文摘要',
    response_text  TEXT                     COMMENT 'AI响应',
    latency_ms     INT,
    is_adopted     TINYINT(1)      DEFAULT 0 COMMENT '0未采纳 1已采纳',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     BIGINT UNSIGNED,
    PRIMARY KEY (id),
    KEY idx_scene_user (scene, user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史';
