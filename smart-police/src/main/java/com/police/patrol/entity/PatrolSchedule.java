package com.police.patrol.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 巡逻排班
 */
@Data
@TableName("patrol_schedule")
public class PatrolSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long officerId;          // 警员ID
    private LocalDate scheduleDate;  // 排班日期
    private String shiftType;        // morning / afternoon / night
    private LocalTime startTime;     // 班次开始时间
    private LocalTime endTime;       // 班次结束时间
    private Long deptId;
    private String status;           // normal / adjusted / leave
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 非DB字段，联查用 */
    @TableField(exist = false)
    private String officerName;

    @TableField(exist = false)
    private String badgeNo;
}
