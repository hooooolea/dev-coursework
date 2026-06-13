package com.police.vehicle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆布控
 */
@Data
@TableName("vehicle_control")
public class VehicleControl {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;
    private String controlReason;
    private Integer controlLevel;    // 1一般 2紧急
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long relatedCaseId;
    private Integer status;          // 1布控中 0已解控
    private LocalDateTime decontrolTime;
    private String decontrolReason;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
