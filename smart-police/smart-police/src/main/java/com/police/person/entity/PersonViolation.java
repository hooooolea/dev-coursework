package com.police.person.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人员违法记录
 */
@Data
@TableName("person_violation")
public class PersonViolation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long personId;
    private LocalDateTime violationTime;
    private String violationType;
    private String description;
    private String result;
    private BigDecimal fineAmount;
    private Integer detentionDays;
    private Long relatedCaseId;
    private Long handlerId;
    private Long deptId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
