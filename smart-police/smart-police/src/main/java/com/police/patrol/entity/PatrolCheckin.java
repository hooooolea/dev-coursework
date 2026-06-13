package com.police.patrol.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patrol_checkin")
public class PatrolCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long officerId;
    private String checkinType;
    private LocalDateTime checkinTime;
    private String location;
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
