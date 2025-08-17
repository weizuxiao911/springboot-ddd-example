package com.ylz.framework.domain.user;

/**
 * 用户仓储接口
 * @author weizuxiao
 */
public interface UserRepository {
    
    User findById(UserId userId);

    User save(User user);

}
