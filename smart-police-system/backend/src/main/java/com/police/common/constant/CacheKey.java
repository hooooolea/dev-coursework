package com.police.common.constant;

/**
 * Redis 缓存 Key 常量
 */
public interface CacheKey {

    /** 用户Token: token:{userId} */
    String TOKEN_PREFIX = "token:";

    /** Token 黑名单: blacklist:{token} */
    String BLACKLIST_PREFIX = "blacklist:";

    /** 菜单权限树: menu:{roleId} */
    String MENU_PREFIX = "menu:";

    /** 数据字典: dict:{type} */
    String DICT_PREFIX = "dict:";

    /** 案件统计: stat:case:{date} */
    String STAT_CASE_PREFIX = "stat:case:";

    /** 接口限流: rate:{ip}:{api} */
    String RATE_PREFIX = "rate:";

    /** 登录失败次数: login:fail:{username} */
    String LOGIN_FAIL_PREFIX = "login:fail:";

    /** 账号锁定: login:lock:{username} */
    String LOGIN_LOCK_PREFIX = "login:lock:";

    // ---- 过期时间（秒）----
    int TOKEN_EXPIRE = 7200;
    int MENU_EXPIRE = 86400;
    int DICT_EXPIRE = 43200;
    int STAT_EXPIRE = 3600;
    int RATE_EXPIRE = 60;
    int LOGIN_FAIL_EXPIRE = 1800;   // 30分钟
}
