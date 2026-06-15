package com.police.caseinfo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案件嫌疑人
 */
@Data
@TableName("case_suspect")
public class CaseSuspect {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long caseId;
    private String name;
    private String idCard;
    private String phone;
    private String gender;            // male / female / unknown
    @TableField(exist = false)
    private Integer age;
    private String address;
    @TableField(exist = false)
    private String suspectRole;       // 主犯/从犯/嫌疑人/在逃/已抓获 等
    @TableField(exist = false)
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    @TableField(exist = false)
    private Integer isDeleted;
}
