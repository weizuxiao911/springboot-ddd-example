package com.ylz.framework.example.interfaces.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ylz.framework.application.user.dto.UserDTO;

@FeignClient(name = "user-service", url = "user-service")
public interface UserFeignClient {
    @GetMapping("/api/v1/users/{userId}")
    UserDTO getUserById(@PathVariable String userId);
}
