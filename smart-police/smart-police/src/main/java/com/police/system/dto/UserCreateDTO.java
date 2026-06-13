package com.police.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateDTO {

    @NotBlank(message = "警号不能为空")
    private String badgeNo;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private String phone;
    private String email;
    private Long deptId;
    private String remark;

    /** 分配角色 ID 列表 */
    private List<Long> roleIds;
}
