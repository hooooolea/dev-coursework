package com.police.officer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("officer_info")
public class OfficerInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String badgeNo;
    private String realName;
    private Integer gender;
    private String policeCategory;
    private String rankTitle;
    private String position;
    private Long deptId;
    private LocalDate entryDate;
    private String idCard;
    private String phone;
    private String emergencyContact;
    private String emergencyPhone;
    private String skills;
    private String workStatus;
    private String photoUrl;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
