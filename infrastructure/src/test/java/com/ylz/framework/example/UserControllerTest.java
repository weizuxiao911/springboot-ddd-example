package com.ylz.framework.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ylz.framework.application.user.command.CreateCommand;
import com.ylz.framework.example.interfaces.controller.UserController;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    void testCreateUser() {
        CreateCommand command = new CreateCommand("测试", "");
        userController.createUser(command);
    }
    
}
