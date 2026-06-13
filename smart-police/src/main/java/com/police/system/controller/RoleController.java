package com.police.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.system.entity.SysPermission;
import com.police.system.entity.SysRole;
import com.police.system.mapper.SysPermissionMapper;
import com.police.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permMapper;
    private final JdbcTemplate jdbc;

    /** 角色列表 */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public Result<List<SysRole>> list() {
        return Result.ok(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getIsDeleted, 0).orderByAsc(SysRole::getSortOrder)));
    }

    /** 新增角色 */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:add')")
    @OperationLog(module = "系统管理", action = "新增角色")
    public Result<Long> create(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.ok(role.getId());
    }

    /** 编辑角色 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog(module = "系统管理", action = "编辑角色")
    public Result<?> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.ok();
    }

    /** 删除角色 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:del')")
    @OperationLog(module = "系统管理", action = "删除角色")
    public Result<?> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.ok();
    }

    /** 查询角色已分配的权限ID列表 */
    @GetMapping("/{id}/perms")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public Result<List<Long>> getPerms(@PathVariable Long id) {
        List<Long> ids = jdbc.queryForList(
                "SELECT permission_id FROM sys_role_permission WHERE role_id = ?", Long.class, id);
        return Result.ok(ids);
    }

    /** 分配权限（全量覆盖） */
    @PutMapping("/{id}/perms")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog(module = "系统管理", action = "分配权限")
    public Result<?> assignPerms(@PathVariable Long id, @RequestBody List<Long> permIds) {
        jdbc.update("DELETE FROM sys_role_permission WHERE role_id = ?", id);
        if (permIds != null && !permIds.isEmpty()) {
            for (Long permId : permIds) {
                jdbc.update("INSERT INTO sys_role_permission(role_id, permission_id) VALUES(?,?)", id, permId);
            }
        }
        return Result.ok();
    }

    /** 权限树（所有权限，树形结构） */
    @GetMapping("/perm-tree")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public Result<List<SysPermission>> permTree() {
        List<SysPermission> all = permMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getIsDeleted, 0)
                        .eq(SysPermission::getStatus, 1).orderByAsc(SysPermission::getSortOrder));
        return Result.ok(buildPermTree(all, 0L));
    }

    private List<SysPermission> buildPermTree(List<SysPermission> all, Long parentId) {
        List<SysPermission> children = new ArrayList<>();
        for (SysPermission p : all) {
            if (parentId.equals(p.getParentId())) {
                p.setChildren(buildPermTree(all, p.getId()));
                children.add(p);
            }
        }
        return children;
    }
}
