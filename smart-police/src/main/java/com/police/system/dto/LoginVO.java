package com.police.system.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String accessToken;
    private String refreshToken;
    private List<String> roles;
    private List<String> permissions;
}
