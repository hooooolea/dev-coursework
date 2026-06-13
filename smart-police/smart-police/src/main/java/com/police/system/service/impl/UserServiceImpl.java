package com.police.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.common.exception.BusinessException;
import com.police.system.dto.UserCreateDTO;
import com.police.system.dto.UserQueryDTO;
import com.police.system.entity.SysUser;
import com.police.system.mapper.SysUserMapper;
import com.police.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<SysUser> listPage(UserQueryDTO query) {
        Page<SysUser> page = new Page<>(query.getPage(), query.getSize());
        return baseMapper.selectUserPage(page, query);
    }

    @Override
    public Long createUser(UserCreateDTO dto) {
        // 1. 校验警号/用户名唯一性
        if (existsByBadgeNo(dto.getBadgeNo())) {
            throw BusinessException.of("警号 " + dto.getBadgeNo() + " 已存在");
        }
        if (existsByUsername(dto.getUsername())) {
            throw BusinessException.of("用户名 " + dto.getUsername() + " 已存在");
        }

        // 2. 创建用户
        SysUser user = new SysUser();
        user.setBadgeNo(dto.getBadgeNo());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setGender(dto.getGender());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setRemark(dto.getRemark());
        user.setStatus(1);
        user.setLoginFailCount(0);
        save(user);

        return user.getId();
    }

    @Override
    public void updateUser(Long id, UserCreateDTO dto) {
        SysUser user = getById(id);
        if (user == null) throw BusinessException.of("用户不存在");

        user.setRealName(dto.getRealName());
        user.setGender(dto.getGender());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setRemark(dto.getRemark());
        updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        removeById(id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        if (user == null) throw BusinessException.of("用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        updateById(user);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) throw BusinessException.of("用户不存在");
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public SysUser loadUserWithPerms(String username) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0));
        if (user == null) return null;
        // 注入权限
        List<String> perms = baseMapper.selectPermCodesByUserId(user.getId());
        List<String> roles = baseMapper.selectRoleCodesByUserId(user.getId());
        user.setPermissions(perms);
        user.setRoleCodes(roles);
        return user;
    }

    private boolean existsByBadgeNo(String badgeNo) {
        return count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getBadgeNo, badgeNo)) > 0;
    }

    private boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }
}
