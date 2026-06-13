package com.police.caseinfo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 案件证据
 */
@Data
@TableName("case_evidence")
public class CaseEvidence {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long caseId;
    private String evidenceNo;
    private String evidenceName;
    private String evidenceType;
    private LocalDateTime collectTime;
    private String collectLocation;
    private Long collectorId;
    private String storageLocation;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer status;         // 1正常 2已销毁

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
