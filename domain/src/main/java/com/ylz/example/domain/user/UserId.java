package com.ylz.example.domain.user;

import java.util.UUID;

/**
 * 用户值对象
 * 
 * @author weizuxiao
 */
public record UserId(String value) {

    /**
     * 生成符合规则的用户ID（10位字符串）
     * 实现方式：取UUID的前8位 + 2位随机数，确保长度为10位
     */
    public static UserId generate() {
        String uuidPrefix = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        int randomNum = (int) (Math.random() * 100); // 0-99的随机数
        String userIdStr = uuidPrefix + String.format("%02d", randomNum);
        return new UserId(userIdStr);
    }

    // 重写toString，仅返回值（可选，根据需要）
    @Override
    public String toString() {
        return value;
    }
}