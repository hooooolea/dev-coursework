package com.police.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.common.annotation.OperationLog;
import com.police.common.exception.BusinessException;
import com.police.common.result.Result;
import com.police.equipment.entity.EquipmentBorrow;
import com.police.equipment.entity.EquipmentInfo;
import com.police.equipment.mapper.EquipmentBorrowMapper;
import com.police.equipment.mapper.EquipmentInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentInfoMapper infoMapper;
    private final EquipmentBorrowMapper borrowMapper;

    /* ====== 装备档案 ====== */

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<EquipmentInfo>> list(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String equipType) {

        LambdaQueryWrapper<EquipmentInfo> q = new LambdaQueryWrapper<EquipmentInfo>()
                .eq(EquipmentInfo::getIsDeleted, 0)
                .orderByAsc(EquipmentInfo::getEquipNo);
        if (keyword  != null && !keyword.isBlank())
            q.and(w -> w.like(EquipmentInfo::getEquipName, keyword).or().like(EquipmentInfo::getEquipNo, keyword));
        if (status   != null && !status.isBlank())   q.eq(EquipmentInfo::getStatus, status);
        if (equipType != null && !equipType.isBlank()) q.eq(EquipmentInfo::getEquipType, equipType);

        return Result.ok(infoMapper.selectPage(new Page<>(page, size), q));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "装备管理", action = "新增装备")
    public Result<Long> create(@RequestBody EquipmentInfo info) {
        if (info.getStatus() == null) info.setStatus("idle");
        infoMapper.insert(info);
        return Result.ok(info.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "装备管理", action = "编辑装备")
    public Result<?> update(@PathVariable Long id, @RequestBody EquipmentInfo info) {
        info.setId(id);
        infoMapper.updateById(info);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "装备管理", action = "删除装备")
    public Result<?> delete(@PathVariable Long id) {
        // 借出中不允许删除
        long borrowing = borrowMapper.selectCount(new LambdaQueryWrapper<EquipmentBorrow>()
                .eq(EquipmentBorrow::getEquipmentId, id).eq(EquipmentBorrow::getStatus, 1));
        if (borrowing > 0) throw BusinessException.of("该装备当前处于借出状态，无法删除");
        infoMapper.deleteById(id);
        return Result.ok();
    }

    /* ====== 借还记录 ====== */

    @GetMapping("/{id}/borrows")
    @PreAuthorize("isAuthenticated()")
    public Result<List<EquipmentBorrow>> borrows(@PathVariable Long id) {
        return Result.ok(borrowMapper.selectByEquipmentId(id));
    }

    @PostMapping("/{id}/borrow")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "装备管理", action = "装备领用")
    public Result<Long> borrow(@PathVariable Long id, @RequestBody EquipmentBorrow record) {
        EquipmentInfo info = infoMapper.selectById(id);
        if (info == null) throw BusinessException.of("装备不存在");
        if (!"idle".equals(info.getStatus())) throw BusinessException.of("装备当前状态不可领用：" + info.getStatus());

        record.setEquipmentId(id);
        record.setStatus(1);
        record.setBorrowTime(LocalDateTime.now());
        borrowMapper.insert(record);

        // 更新装备状态为借出
        EquipmentInfo upd = new EquipmentInfo();
        upd.setId(id);
        upd.setStatus("borrowed");
        infoMapper.updateById(upd);

        return Result.ok(record.getId());
    }

    @PutMapping("/borrow/{borrowId}/return")
    @PreAuthorize("isAuthenticated()")
    @OperationLog(module = "装备管理", action = "装备归还")
    public Result<?> returnEquip(@PathVariable Long borrowId, @RequestBody Map<String, String> body) {
        EquipmentBorrow record = borrowMapper.selectById(borrowId);
        if (record == null) throw BusinessException.of("借还记录不存在");

        record.setStatus(2);
        record.setActualReturn(LocalDateTime.now());
        record.setReturnNote(body.get("returnNote"));
        borrowMapper.updateById(record);

        // 更新装备状态为空闲
        EquipmentInfo upd = new EquipmentInfo();
        upd.setId(record.getEquipmentId());
        upd.setStatus("idle");
        infoMapper.updateById(upd);

        return Result.ok();
    }
}
