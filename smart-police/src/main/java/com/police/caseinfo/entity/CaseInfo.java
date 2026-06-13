package com.police.caseinfo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 案件信息
 */
@Data
@TableName("case_info")
public class CaseInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String caseNo;           // 案件编号
    private String caseName;         // 案件名称
    private String caseCategory;     // 案件大类 criminal/public/traffic
    private String caseType;         // 案件小类（字典）
    private LocalDateTime occurredAt;// 发案时间
    private String locationProvince;
    private String locationCity;
    private String locationDistrict;
    private String locationDetail;
    private String caseDesc;         // 案情描述
    private String status;           // pending/investigating/transferred/closed/cancelled
    private Integer severityLevel;   // 1一般 2重要 3重大 4特重大
    private Long leadOfficerId;      // 主办民警
    private Long deptId;
    private LocalDate fileDate;      // 立案日期
    private LocalDate closedDate;    // 结案日期
    private String cancelledReason;
    private LocalDate deadlineDate;  // 办案期限
    private Integer isOverdue;       // 是否超期
    private Long relatedAlarmId;
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
