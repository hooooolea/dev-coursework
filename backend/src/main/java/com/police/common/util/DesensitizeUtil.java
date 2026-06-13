package com.police.common.util;

/**
 * 数据脱敏工具类
 */
public class DesensitizeUtil {

    /**
     * 手机号脱敏: 150****8888
     */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 身份证脱敏: 310101********001X
     */
    public static String idCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 姓名脱敏: 张* 或 张**
     */
    public static String name(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() == 1) return name;
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 银行卡脱敏: **** **** **** 1234
     */
    public static String bankCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) return cardNo;
        return "**** **** **** " + cardNo.substring(cardNo.length() - 4);
    }

    /**
     * 邮箱脱敏: a***@example.com
     */
    public static String email(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String prefix = parts[0].length() > 1 ? parts[0].charAt(0) + "***" : parts[0];
        return prefix + "@" + parts[1];
    }
}
