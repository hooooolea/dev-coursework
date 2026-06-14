package com.police.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.police.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 按角色ID列表查询权限 */
    List<SysPermission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}
