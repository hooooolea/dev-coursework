package com.police.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
