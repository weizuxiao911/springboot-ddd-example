package com.ylz.example.domain.user;

/**
 * 用户仓储接口
 * @author weizuxiao
 */
public interface UserRepository {

    /**
     * 根据 ID 查询用户
     * @param userId
     * @return
     */
    User findById(UserId userId);

    /**
     * 保存用户
     * @param user
     * @return
     */
    User save(User user);

}
