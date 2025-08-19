package com.ylz.example.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.ylz.example.api.controller.UserController;
import com.ylz.example.application.dto.CreateUserRequest;

@SpringBootTest
@Transactional
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    @Rollback(true) // 回滚事务
    // @Rollback(false) // 提交事务
    void testCreateUser() {
        CreateUserRequest request = new CreateUserRequest("测试", "");
        userController.createUser(request);
    }

}
