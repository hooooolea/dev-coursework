package com.police.caseinfo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案件侦查进展
 */
@Data
@TableName("case_progress")
public class CaseProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long caseId;
    private LocalDateTime progressTime;
    private String content;
    private String nextPlan;
    private Long officerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
