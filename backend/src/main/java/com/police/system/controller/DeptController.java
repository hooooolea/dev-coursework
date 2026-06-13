package com.police.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.common.annotation.OperationLog;
import com.police.common.exception.BusinessException;
import com.police.common.result.Result;
import com.police.system.entity.SysDept;
import com.police.system.mapper.SysDeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
public class DeptController {

    private final SysDeptMapper deptMapper;

    /** 部门树（含子节点） */
    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        List<SysDept> all = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getIsDeleted, 0).orderByAsc(SysDept::getSortOrder));
        return Result.ok(buildTree(all, 0L));
    }

    /** 扁平列表（用于下拉选择） */
    @GetMapping("/list")
    public Result<List<SysDept>> list() {
        return Result.ok(deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getIsDeleted, 0).orderByAsc(SysDept::getSortOrder)));
    }

    /** 新增部门 */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:dept:add')")
    @OperationLog(module = "系统管理", action = "新增部门")
    public Result<Long> create(@RequestBody SysDept dept) {
        if (dept.getParentId() == null) dept.setParentId(0L);
        deptMapper.insert(dept);
        return Result.ok(dept.getId());
    }

    /** 编辑部门 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog(module = "系统管理", action = "编辑部门")
    public Result<?> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptMapper.updateById(dept);
        return Result.ok();
    }

    /** 删除部门（有子部门时拒绝） */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:dept:del')")
    @OperationLog(module = "系统管理", action = "删除部门")
    public Result<?> delete(@PathVariable Long id) {
        long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id).eq(SysDept::getIsDeleted, 0));
        if (childCount > 0) throw BusinessException.of("请先删除子部门");
        deptMapper.deleteById(id);
        return Result.ok();
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        List<SysDept> children = new ArrayList<>();
        for (SysDept dept : all) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(buildTree(all, dept.getId()));
                children.add(dept);
            }
        }
        return children;
    }
}
