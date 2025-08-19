package com.ylz.example.infrastructure.adapter;

import org.springframework.stereotype.Service;

import com.ylz.example.application.impl.UserAppServiceImpl;
import com.ylz.example.domain.user.UserRepository;

import jakarta.transaction.Transactional;

/**
 * 技术适配层
 */
@Transactional
@Service
public class UserAppServiceAdpter extends UserAppServiceImpl {

    public UserAppServiceAdpter(UserRepository userRepository) {
        super(userRepository);
    }

}
