package com.police.equipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_borrow")
public class EquipmentBorrow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long equipmentId;
    private Long borrowerId;
    private String borrowPurpose;
    private LocalDateTime borrowTime;
    private LocalDateTime expectedReturn;
    private LocalDateTime actualReturn;
    private Integer status;          // 1借出中 2已归还
    private String returnNote;
    private Long approverId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 联查字段 */
    @TableField(exist = false)
    private String equipName;

    @TableField(exist = false)
    private String equipNo;

    @TableField(exist = false)
    private String borrowerName;
}
