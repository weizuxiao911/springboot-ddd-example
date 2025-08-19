package com.ylz.example.domain.user;

import com.ylz.framework.asm.MethodReferenceUpdate;

import lombok.Getter;

/**
 * 用户领域实体
 * 
 * @author weizuxiao
 */
@Getter
public class User implements MethodReferenceUpdate {

    /**
     * 用户 ID
     */
    private final UserId id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    private User(final UserId id) {
        this.id = id;
    }

    public User(UserId id, String nickname, String avatar) {
        this.id = id;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    /**
     * 创建用户
     * @param userId
     * @return
     */
    public static User create(final UserId userId) {
        return new User(userId);
    }

    /**
     * 编辑属性
     * @param <T>
     * @param fieldGetter
     * @param value
     */
    public <T> void editNickname(String value) {
      
    }

}
