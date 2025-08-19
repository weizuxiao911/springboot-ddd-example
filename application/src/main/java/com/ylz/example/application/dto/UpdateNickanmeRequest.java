package com.ylz.example.application.dto;

/**
 * 编辑昵称请求
 * 
 * @author weizuxiao
 */
public record UpdateNickanmeRequest(
                String userId,
                String nickname) {

}
