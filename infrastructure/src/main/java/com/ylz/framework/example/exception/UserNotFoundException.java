package com.ylz.framework.example.exception;

import com.ylz.framework.domain.user.UserId;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UserId userId) {
       
    }
    
}
