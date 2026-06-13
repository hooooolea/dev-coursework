package com.police.vehicle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.common.exception.BusinessException;
import com.police.common.util.SecurityUtil;
import com.police.vehicle.entity.VehicleControl;
import com.police.vehicle.entity.VehicleInfo;
import com.police.vehicle.mapper.VehicleControlMapper;
import com.police.vehicle.mapper.VehicleInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class VehicleServiceImpl extends ServiceImpl<VehicleInfoMapper, VehicleInfo> {

    private final VehicleControlMapper controlMapper;

    public Long create(VehicleInfo vehicle) {
        if (existsByPlate(vehicle.getPlateNo())) {
            throw BusinessException.of("车牌号 " + vehicle.getPlateNo() + " 已存在");
        }
        vehicle.setStatus("normal");
        save(vehicle);
        return vehicle.getId();
    }

    public IPage<VehicleInfo> listPage(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<VehicleInfo>()
                .eq(VehicleInfo::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(VehicleInfo::getPlateNo, keyword)
                   .or().like(VehicleInfo::getOwnerName, keyword);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(VehicleInfo::getStatus, status);
        }
        return page(new Page<>(page, size), wrapper);
    }

    public void control(Long vehicleId, String reason, Integer level, Long caseId) {
        VehicleInfo v = getById(vehicleId);
        if (v == null) throw BusinessException.of("车辆不存在");

        VehicleControl ctrl = new VehicleControl();
        ctrl.setVehicleId(vehicleId);
        ctrl.setControlReason(reason);
        ctrl.setControlLevel(level);
        ctrl.setStartTime(LocalDateTime.now());
        ctrl.setRelatedCaseId(caseId);
        ctrl.setStatus(1);
        ctrl.setOperatorId(SecurityUtil.getCurrentUserId());
        controlMapper.insert(ctrl);

        v.setStatus("controlled");
        updateById(v);
    }

    public void decontrol(Long vehicleId, String reason) {
        VehicleInfo v = getById(vehicleId);
        if (v == null) throw BusinessException.of("车辆不存在");

        // 关闭布控记录
        controlMapper.selectList(new LambdaQueryWrapper<VehicleControl>()
                .eq(VehicleControl::getVehicleId, vehicleId)
                .eq(VehicleControl::getStatus, 1))
                .forEach(c -> {
                    c.setStatus(0);
                    c.setDecontrolTime(LocalDateTime.now());
                    c.setDecontrolReason(reason);
                    controlMapper.updateById(c);
                });

        v.setStatus("normal");
        updateById(v);
    }

    private boolean existsByPlate(String plateNo) {
        return count(new LambdaQueryWrapper<VehicleInfo>()
                .eq(VehicleInfo::getPlateNo, plateNo)) > 0;
    }
}
