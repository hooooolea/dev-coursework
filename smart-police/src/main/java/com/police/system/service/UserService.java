package com.police.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.police.system.dto.UserCreateDTO;
import com.police.system.dto.UserQueryDTO;
import com.police.system.entity.SysUser;

public interface UserService extends IService<SysUser> {

    /** 分页查询用户 */
    IPage<SysUser> listPage(UserQueryDTO query);

    /** 新增用户 */
    Long createUser(UserCreateDTO dto);

    /** 更新用户 */
    void updateUser(Long id, UserCreateDTO dto);

    /** 删除用户（软删） */
    void deleteUser(Long id);

    /** 重置密码 */
    void resetPassword(Long id, String newPassword);

    /** 启用/禁用账号 */
    void toggleStatus(Long id, Integer status);

    /** 根据用户名加载用户（带权限） */
    SysUser loadUserWithPerms(String username);
}
