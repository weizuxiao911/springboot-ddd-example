package com.ylz.example.application.dto;

/**
 * 创建用户请求
 * 
 * @author weizuxiao
 */
public record CreateUserRequest(
        String nickname,
        String avatar) {

}
