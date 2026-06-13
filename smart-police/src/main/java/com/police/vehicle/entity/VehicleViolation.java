package com.police.vehicle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆违章记录
 */
@Data
@TableName("vehicle_violation")
public class VehicleViolation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;
    private LocalDateTime violationTime;
    private String violationType;       // 违章类型（字典值）
    private String location;            // 违章地点
    private BigDecimal fineAmount;      // 罚款金额
    private Integer deductedPoints;     // 扣分
    private Integer isPaid;             // 0未缴 1已缴
    private LocalDateTime paidAt;
    private Long relatedCaseId;
    private String evidenceUrl;
    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;
}
