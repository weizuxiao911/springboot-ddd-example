package com.ylz.example.application.dto;


/**
 * 用户DTO
 * 
 * @author weizuxiao
 */
public record UserResponse(
                String userId,
                String nickname,
                String avatar) {
}
