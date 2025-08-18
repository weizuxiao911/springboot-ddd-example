package com.ylz.framework.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ylz.framework.application.dto.CreateUserRequest;
import com.ylz.framework.example.api.controller.UserController;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    void testCreateUser() {
        CreateUserRequest command = new CreateUserRequest("测试", "");
        userController.createUser(command);
    }
    
}
