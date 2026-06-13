package com.police.alarm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 警情派发
 */
@Data
@TableName("alarm_dispatch")
public class AlarmDispatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long alarmId;
    private Long officerId;
    private LocalDateTime dispatchTime;
    private LocalDateTime expectedArrive;
    private LocalDateTime arriveTime;
    private Integer status;         // 1待接收 2已接收 3处置中 4已完成
    private String resultDesc;
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
