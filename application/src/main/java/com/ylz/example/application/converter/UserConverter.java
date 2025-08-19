package com.ylz.example.application.converter;

import com.ylz.example.application.dto.UserResponse;
import com.ylz.example.domain.user.User;

public class UserConverter {
    // 领域对象 → DTO
    public static UserResponse toDTO(User user) {
        return new UserResponse(
            user.getId().value(),
            user.getNickname(),
            user.getAvatar()
        );
    }
}

