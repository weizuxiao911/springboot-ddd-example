package com.ylz.framework.application.dto;

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
