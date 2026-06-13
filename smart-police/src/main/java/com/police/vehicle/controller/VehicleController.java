package com.police.vehicle.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.common.annotation.OperationLog;
import com.police.common.result.Result;
import com.police.vehicle.entity.VehicleInfo;
import com.police.vehicle.entity.VehicleViolation;
import com.police.vehicle.mapper.VehicleViolationMapper;
import com.police.vehicle.service.impl.VehicleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleServiceImpl vehicleService;
    private final VehicleViolationMapper violationMapper;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('vehicle:view')")
    public Result<IPage<VehicleInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.ok(vehicleService.listPage(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('vehicle:view')")
    public Result<VehicleInfo> detail(@PathVariable Long id) {
        return Result.ok(vehicleService.getById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('vehicle:view')")
    public Result<VehicleInfo> searchByPlate(@RequestParam String plate) {
        return Result.ok(vehicleService.lambdaQuery()
                .eq(VehicleInfo::getPlateNo, plate).one());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('vehicle:add')")
    @OperationLog(module = "车辆管理", action = "登记车辆")
    public Result<Long> create(@RequestBody VehicleInfo vehicle) {
        return Result.ok(vehicleService.create(vehicle));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('vehicle:edit')")
    @OperationLog(module = "车辆管理", action = "编辑车辆")
    public Result<?> update(@PathVariable Long id, @RequestBody VehicleInfo vehicle) {
        vehicle.setId(id);
        vehicleService.updateById(vehicle);
        return Result.ok();
    }

    @PostMapping("/{id}/control")
    @PreAuthorize("hasAuthority('vehicle:control')")
    @OperationLog(module = "车辆管理", action = "设置布控")
    public Result<?> control(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        vehicleService.control(id,
                (String) body.get("reason"),
                (Integer) body.get("level"),
                body.get("caseId") != null ? Long.parseLong(body.get("caseId").toString()) : null);
        return Result.ok();
    }

    @PutMapping("/{id}/decontrol")
    @PreAuthorize("hasAuthority('vehicle:control')")
    @OperationLog(module = "车辆管理", action = "解除布控")
    public Result<?> decontrol(@PathVariable Long id, @RequestBody Map<String, String> body) {
        vehicleService.decontrol(id, body.get("reason"));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('vehicle:del')")
    @OperationLog(module = "车辆管理", action = "删除车辆")
    public Result<?> delete(@PathVariable Long id) {
        vehicleService.removeById(id);
        return Result.ok();
    }

    /* ====== 违章记录 ====== */

    @GetMapping("/{id}/violations")
    @PreAuthorize("hasAuthority('vehicle:view')")
    public Result<List<VehicleViolation>> listViolations(@PathVariable Long id) {
        return Result.ok(violationMapper.selectList(
                new LambdaQueryWrapper<VehicleViolation>()
                        .eq(VehicleViolation::getVehicleId, id)
                        .orderByDesc(VehicleViolation::getViolationTime)));
    }

    @PostMapping("/{id}/violations")
    @PreAuthorize("hasAuthority('vehicle:edit')")
    @OperationLog(module = "车辆管理", action = "新增违章记录")
    public Result<Long> addViolation(@PathVariable Long id, @RequestBody VehicleViolation violation) {
        violation.setVehicleId(id);
        violationMapper.insert(violation);
        return Result.ok(violation.getId());
    }

    @PutMapping("/violations/{violationId}/pay")
    @PreAuthorize("hasAuthority('vehicle:edit')")
    @OperationLog(module = "车辆管理", action = "标记违章已缴款")
    public Result<?> markPaid(@PathVariable Long violationId) {
        VehicleViolation v = new VehicleViolation();
        v.setId(violationId);
        v.setIsPaid(1);
        v.setPaidAt(java.time.LocalDateTime.now());
        violationMapper.updateById(v);
        return Result.ok();
    }
}
