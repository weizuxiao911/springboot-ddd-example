package com.ylz.framework.application.user.command;

/**
 * 编辑昵称请求
 * 
 * @author weizuxiao
 */
public record UpdateNickanmeCommand(
                String userId,
                String nickname) {

}
