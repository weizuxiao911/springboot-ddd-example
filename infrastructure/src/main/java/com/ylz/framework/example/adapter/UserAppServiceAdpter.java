package com.ylz.framework.example.adapter;

import org.springframework.stereotype.Service;

import com.ylz.framework.application.impl.UserAppServiceImpl;
import com.ylz.framework.domain.user.UserRepository;

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
