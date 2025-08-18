package com.ylz.framework.application.converter;

import com.ylz.framework.application.dto.UserResponse;
import com.ylz.framework.domain.user.User;

public class UserConverter {
    // 领域对象 → DTO
    public static UserResponse toDTO(User user) {
        return new UserResponse(
            user.getUserId().value(),
            user.getNickname(),
            user.getAvatar()
        );
    }
}

