package com.ylz.example.application.impl;

import com.ylz.example.application.converter.UserConverter;
import com.ylz.example.application.dto.CreateUserRequest;
import com.ylz.example.application.dto.UpdateNickanmeRequest;
import com.ylz.example.application.dto.UserResponse;
import com.ylz.example.application.service.UserAppService;
import com.ylz.example.domain.user.User;
import com.ylz.example.domain.user.UserId;
import com.ylz.example.domain.user.UserRepository;

public class UserAppServiceImpl implements UserAppService {

    private final UserRepository userRepository;

    public UserAppServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getUserById(String userId) {
        User user = this.userRepository.findById(new UserId(userId));
        return UserConverter.toDTO(user);
    }

    @Override
    public UserResponse createUser(CreateUserRequest command) {
        // 1. 根据命令创建领域对象
        User user = new User(UserId.generate(), command.nickname(), command.avatar());
        // 3. 持久化
        User savedUser = userRepository.save(user);
        // 4. 转换为DTO返回
        return UserConverter.toDTO(savedUser);
    }

    @Override
    public void updateUserNickname(UpdateNickanmeRequest command) {
        // 1. 转换参数（DTO → 领域对象）
        UserId userId = new UserId(command.userId());
        // 2. 调用领域逻辑
        User user = userRepository.findById(userId);
        user.update(User::getNickname, command.nickname()); // 领域对象自身的业务规则
        // 3. 持久化结果
        userRepository.save(user);
    }

}