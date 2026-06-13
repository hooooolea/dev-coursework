package com.police.system.service.impl;

import com.police.system.entity.SysUser;
import com.police.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用户加载服务
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.loadUserWithPerms(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }
}
