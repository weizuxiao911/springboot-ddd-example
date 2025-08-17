package com.ylz.framework.domain.user;

import java.lang.reflect.Field;
import java.util.function.Function;

import lombok.Getter;

/**
 * 用户领域实体
 * 
 * @author weizuxiao
 */
@Getter
public class User {

    private final UserId userId;

    private String nickname;

    private String avatar;

    private User(final UserId userId) {
        this.userId = userId;
    }

    public User(UserId userId, String nickname, String avatar) {
        this.userId = userId;
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
    public <T> void edit(Function<User, T> fieldGetter, T value) {
        try {
            // 获取方法引用对应的字段名
            String methodName = fieldGetter.getClass().getDeclaredMethod("apply").getName();
            String fieldName = methodName.startsWith("get")
                    ? Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4)
                    : methodName;
            // 通过反射设置字段值
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to edit field", e);
        }
    }

}
