package com.police.system.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String keyword;     // 姓名或警号
    private Long deptId;
    private Integer status;
    private Integer page = 1;
    private Integer size = 20;
}
