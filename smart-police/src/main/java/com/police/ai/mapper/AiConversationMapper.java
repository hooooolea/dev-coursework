package com.police.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话历史 Mapper
 *
 * <p>提供 ai_conversation 表的基础 CRUD 操作（MyBatis-Plus）。</p>
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
