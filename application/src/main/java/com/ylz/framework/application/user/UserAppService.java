package com.ylz.framework.application.user;

import com.ylz.framework.application.user.command.CreateCommand;
import com.ylz.framework.application.user.command.UpdateNickanmeCommand;
import com.ylz.framework.application.user.dto.UserDTO;

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
    UserDTO getUserById(String userId);

    /**
     * 创建用户
     * 
     * @param nickname
     * @param avatar
     * @return
     */
    UserDTO createUser(CreateCommand command);

    /**
     * 更新用户昵称
     * 
     * @param command
     */
    void updateUserNickname(UpdateNickanmeCommand command);
}
