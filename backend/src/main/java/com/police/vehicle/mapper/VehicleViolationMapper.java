package com.police.vehicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.vehicle.entity.VehicleViolation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleViolationMapper extends BaseMapper<VehicleViolation> {
}
