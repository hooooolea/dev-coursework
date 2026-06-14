package com.police.officer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 警员绩效考核
 */
@Data
@TableName("officer_assessment")
public class OfficerAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long officerId;
    private String periodType;       // month / quarter / year
    private String periodValue;      // 2024-01 / 2024-Q1 / 2024
    private BigDecimal attendanceScore;
    private BigDecimal caseScore;
    private BigDecimal violationScore;
    private BigDecimal rewardScore;
    private BigDecimal totalScore;
    private String result;           // excellent / good / pass / fail
    private Long assessorId;
    private LocalDate assessDate;
    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
