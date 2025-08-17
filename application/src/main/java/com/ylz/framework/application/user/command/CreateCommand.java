package com.ylz.framework.application.user.command;

/**
 * 创建用户请求
 * 
 * @author weizuxiao
 */
public record CreateCommand(
        String nickname,
        String avatar) {

}
