package com.ylz.framework.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ylz.framework.application.user.UserAppService;
import com.ylz.framework.application.user.command.CreateCommand;
import com.ylz.framework.application.user.command.UpdateNickanmeCommand;
import com.ylz.framework.application.user.dto.UserDTO;
import com.ylz.framework.domain.user.User;
import com.ylz.framework.domain.user.UserId;
import com.ylz.framework.domain.user.UserRepository;
import com.ylz.framework.example.interfaces.converter.UserConverter;

@Service
@Transactional
public class UserAppServiceImpl implements UserAppService {

    private final UserRepository userRepository;

    public UserAppServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO getUserById(String userId) {
        User user = this.userRepository.findById(new UserId(userId));
        return UserConverter.toDTO(user);
    }

    @Override
    public UserDTO createUser(CreateCommand command) {
        // 1. 根据命令创建领域对象
        User user = new User(
                UserId.generate(),
                command.nickname(),
                command.avatar()
        // 其他必要属性
        );
        // 2. 可能的领域逻辑处理
        // user.initialize(); // 例如初始化用户状态等
        // 3. 持久化
        User savedUser = userRepository.save(user);
        // 4. 转换为DTO返回
        return UserConverter.toDTO(savedUser);
    }

    @Override
    public void updateUserNickname(UpdateNickanmeCommand command) {
        // 1. 转换参数（DTO → 领域对象）
        UserId userId = new UserId(command.userId());
        // 2. 调用领域逻辑
        User user = userRepository.findById(userId);
        user.edit(User::getNickname, command.nickname()); // 领域对象自身的业务规则
        // 3. 持久化结果
        userRepository.save(user);
    }

}