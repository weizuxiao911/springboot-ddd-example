package com.ylz.example.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ylz.example.application.dto.CreateUserRequest;
import com.ylz.example.application.dto.UserResponse;

@RequestMapping("/api/v1/users")
public interface UserController {

    @PostMapping
    ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request);

    @PutMapping("/{userId}/nickname")
    ResponseEntity<Void> updateNickname(
            @PathVariable String userId,
            @RequestParam String newNickname);

    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable String userId);
}
