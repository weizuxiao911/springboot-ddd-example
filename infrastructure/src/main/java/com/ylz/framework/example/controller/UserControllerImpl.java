package com.ylz.framework.example.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ylz.framework.application.dto.CreateUserRequest;
import com.ylz.framework.application.dto.UpdateNickanmeRequest;
import com.ylz.framework.application.dto.UserResponse;
import com.ylz.framework.application.service.UserAppService;
import com.ylz.framework.example.api.controller.UserController;

@RestController
public class UserControllerImpl implements UserController {

    private final UserAppService userAppService;

    // 构造注入（依赖Spring）
    public UserControllerImpl(UserAppService userApplicationService) {
        this.userAppService = userApplicationService;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest command) {
        // 1. 调用应用服务创建用户（业务逻辑在服务层实现）
        UserResponse createdUser = userAppService.createUser(command);

        // 2. 构建创建资源的URI（符合REST规范）
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // 基于当前请求的URL
                .path("/{id}") // 追加路径参数
                .buildAndExpand(createdUser.userId()) // 替换路径中的{id}
                .toUri();

        // 3. 返回201 Created状态码 + 新创建的用户信息 + Location头
        return ResponseEntity.created(location).body(createdUser);
    }

    @Override
    public ResponseEntity<Void> updateNickname(String userId, String newNickname) {
        userAppService.updateUserNickname(new UpdateNickanmeRequest(userId, newNickname));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UserResponse> getUser(String userId) {
        return ResponseEntity.ok(userAppService.getUserById(userId));
    }

}