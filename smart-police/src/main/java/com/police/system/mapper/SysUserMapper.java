package com.police.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.police.system.dto.UserQueryDTO;
import com.police.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户权限编码列表
     */
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户列表（带部门名称）
     */
    IPage<SysUser> selectUserPage(Page<SysUser> page, @Param("query") UserQueryDTO query);
}
