package com.ylz.framework.example.interfaces.converter;

import com.ylz.framework.application.user.dto.UserDTO;
import com.ylz.framework.domain.user.User;

public class UserConverter {
    // 领域对象 → DTO
    public static UserDTO toDTO(User user) {
        return new UserDTO(
            user.getUserId().value(),
            user.getNickname(),
            user.getAvatar()
        );
    }
}

