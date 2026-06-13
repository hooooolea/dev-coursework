package com.police.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;
    private Integer permType;   // 1菜单 2按钮/接口
    private String permCode;
    private String permName;
    private String icon;
    private String path;
    private String component;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;

    /** 子菜单（非数据库字段，树形结构使用） */
    @TableField(exist = false)
    private List<SysPermission> children;
}
