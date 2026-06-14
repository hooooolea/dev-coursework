package com.police.person.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 人员档案
 */
@Data
@TableName("person_info")
public class PersonInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Integer gender;         // 1男 2女
    private String ethnicity;
    private String idCard;          // AES加密
    private String idCardTail;      // 后4位明文（用于检索）
    private LocalDate birthDate;
    private String phone;           // AES加密
    private String phoneTail;       // 后4位明文
    private String householdAddr;
    private String currentAddr;
    private String occupation;
    private String workUnit;
    private String photoUrl;
    /** normal/focus/wanted/correction/drug */
    private String personType;
    private String typeReason;
    private Long typeOperatorId;
    private LocalDateTime typeAt;
    private String remark;
    private Long deptId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
