package com.police.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String userName;
    private String module;
    private String action;
    private String method;
    private String requestUrl;
    private String requestIp;
    private String requestBody;
    private Integer responseCode;
    private Integer executeTime;
    private LocalDateTime createdAt;
}
