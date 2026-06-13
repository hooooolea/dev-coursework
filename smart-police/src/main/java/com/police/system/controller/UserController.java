package com.police.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.system.dto.UserCreateDTO;
import com.police.system.dto.UserQueryDTO;
import com.police.system.entity.SysRole;
import com.police.system.entity.SysUser;
import com.police.system.mapper.SysRoleMapper;
import com.police.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SysRoleMapper roleMapper;
    private final JdbcTemplate jdbc;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<IPage<SysUser>> list(UserQueryDTO query) {
        return Result.ok(userService.listPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:add')")
    @OperationLog(module = "系统管理", action = "新增用户")
    public Result<Long> create(@RequestBody @Valid UserCreateDTO dto) {
        return Result.ok(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(module = "系统管理", action = "编辑用户")
    public Result<?> update(@PathVariable Long id, @RequestBody @Valid UserCreateDTO dto) {
        userService.updateUser(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    @OperationLog(module = "系统管理", action = "删除用户")
    public Result<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(module = "系统管理", action = "修改用户状态")
    public Result<?> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        userService.toggleStatus(id, body.get("status"));
        return Result.ok();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(module = "系统管理", action = "重置密码")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.ok("密码重置成功");
    }

    /** 查询用户已有角色 */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = jdbc.queryForList(
                "SELECT role_id FROM sys_user_role WHERE user_id = ?", Long.class, id);
        return Result.ok(roleIds);
    }

    /** 全量覆盖用户角色 */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog(module = "系统管理", action = "分配角色")
    public Result<?> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", id);
        if (roleIds != null) {
            for (Long rid : roleIds) {
                jdbc.update("INSERT INTO sys_user_role(user_id, role_id) VALUES(?,?)", id, rid);
            }
        }
        return Result.ok();
    }

    /** 全部角色列表（分配角色弹窗用） */
    @GetMapping("/roles/all")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public Result<List<SysRole>> allRoles() {
        return Result.ok(roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getIsDeleted, 0)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getSortOrder)));
    }
}
