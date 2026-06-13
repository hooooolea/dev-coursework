package com.police.system.controller;

import com.police.common.result.Result;
import com.police.system.dto.LoginDTO;
import com.police.system.dto.LoginVO;
import com.police.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return Result.ok("退出成功");
    }
}
