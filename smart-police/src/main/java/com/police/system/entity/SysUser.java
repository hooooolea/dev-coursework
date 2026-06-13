package com.police.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户实体（同时实现 UserDetails，供 Spring Security 使用）
 */
@Data
@TableName("sys_user")
public class SysUser implements UserDetails {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String badgeNo;
    private String username;

    @JsonIgnore
    private String password;

    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;
    private String avatar;
    private Integer status;            // 1正常 0禁用
    private Integer loginFailCount;
    private LocalDateTime lockUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableLogic
    private Integer isDeleted;

    // ===== 非数据库字段，运行时注入 =====

    /** 用户角色编码列表，登录时查询后注入 */
    @TableField(exist = false)
    @JsonIgnore
    private List<String> roleCodes;

    /** 用户权限编码列表 */
    @TableField(exist = false)
    @JsonIgnore
    private List<String> permissions;

    // ===== UserDetails 接口实现 =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null) return List.of();
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        if (lockUntil == null) return true;
        return LocalDateTime.now().isAfter(lockUntil);
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status != null && status == 1; }
}
