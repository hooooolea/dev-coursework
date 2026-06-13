package com.police.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("equipment_info")
public class EquipmentInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String equipNo;
    private String equipName;
    private String equipType;        // 字典值
    private String brand;
    private String model;
    private LocalDate purchaseDate;
    private Integer usefulLife;
    private BigDecimal purchasePrice;
    private Long deptId;
    private String storageLocation;
    private String status;           // idle / borrowed / maintenance / scrapped
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
