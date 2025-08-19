package com.ylz.example.application.service;

import com.ylz.example.application.dto.CreateUserRequest;
import com.ylz.example.application.dto.UpdateNickanmeRequest;
import com.ylz.example.application.dto.UserResponse;

/**
 * 应用服务接口
 * 
 * @author weizuxiao
 */
public interface UserAppService {

    /**
     * 查询用户详情
     * 
     * @param userId
     * @return
     */
    UserResponse getUserById(String userId);

    /**
     * 创建用户
     * 
     * @param nickname
     * @param avatar
     * @return
     */
    UserResponse createUser(CreateUserRequest command);

    /**
     * 更新用户昵称
     * 
     * @param command
     */
    void updateUserNickname(UpdateNickanmeRequest command);
}
