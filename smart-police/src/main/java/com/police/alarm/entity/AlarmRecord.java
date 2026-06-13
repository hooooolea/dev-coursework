package com.police.alarm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报警记录
 */
@Data
@TableName("alarm_record")
public class AlarmRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alarmNo;          // 警情编号
    private LocalDateTime alarmTime; // 报警时间
    private String callerName;       // 报警人姓名
    private String callerPhone;      // 报警人电话
    private String locationProvince;
    private String locationCity;
    private String locationDistrict;
    private String locationDetail;   // 详细地址
    private String alarmType;        // 警情类型（字典）
    private String alarmDesc;        // 警情描述
    private Integer urgencyLevel;    // 1一般 2较紧急 3紧急 4特急
    private Integer status;          // 1待处置 2处置中 3已处置 4已关闭
    private Long dutyUserId;         // 接警值班员
    private Long relatedCaseId;      // 关联案件
    private LocalDateTime closeTime;
    private String closeSummary;
    private String attachmentUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
