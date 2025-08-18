package com.ylz.framework.example.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ylz.framework.application.dto.UserResponse;

@FeignClient(name = "user-service", url = "user-service")
public interface UserFeignClient {
    @GetMapping("/api/v1/users/{userId}")
    UserResponse getUserById(@PathVariable String userId);
}
