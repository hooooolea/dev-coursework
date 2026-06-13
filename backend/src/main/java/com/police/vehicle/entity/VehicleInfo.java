package com.police.vehicle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆档案
 */
@Data
@TableName("vehicle_info")
public class VehicleInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNo;          // 车牌号
    private String vin;              // 车架号
    private String vehicleType;      // 车辆类型（字典）
    private String brand;
    private String model;
    private String color;
    private Integer year;
    private Long ownerPersonId;      // 登记人员ID
    private String ownerName;        // 登记人姓名（冗余）
    private LocalDate registerDate;
    private LocalDate expireDate;
    private String photoUrl;
    /** normal/suspect/controlled/seized */
    private String status;
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
