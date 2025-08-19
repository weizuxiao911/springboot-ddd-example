package com.ylz.example.infrastructure.exception;

import com.ylz.example.domain.user.UserId;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UserId userId) {
       
    }
    
}
