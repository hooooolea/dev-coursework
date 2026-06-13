package com.police.system.service;

import com.police.system.dto.LoginDTO;
import com.police.system.dto.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    void logout(String token);
}
