package com.police.patrol.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patrol_task")
public class PatrolTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;
    private String taskName;         // 任务名称
    private String taskType;         // routine / special / fixed
    private Long officerId;
    private String areaName;         // 巡逻区域名称
    private LocalDateTime taskStart;
    private LocalDateTime taskEnd;
    private String status;           // pending / accepted / ongoing / completed / cancelled
    private LocalDateTime acceptTime;
    private LocalDateTime completeTime;
    private String summary;          // 巡逻小结
    private Long dispatchBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;
}
