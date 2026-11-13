package com.moneytrasnfer.moneytransferservice.service.impl;

import com.moneytrasnfer.moneytransferservice.entity.UserEntity;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.model.UserRequest;
import com.moneytrasnfer.moneytransferservice.model.UserResponse;
import com.moneytrasnfer.moneytransferservice.repository.UserRepository;
import com.moneytrasnfer.moneytransferservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        // Check if user already exists by uniqueId
        if (userRepository.existsByUniqueId(userRequest.getUniqueId())) {
            log.warn("Attempt to create user with existing unique ID: {}", userRequest.getUniqueId());
            throw new FunctionalException(FunctionalErrorCode.USER_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setContactNumber(userRequest.getContactNumber());
        user.setAddress(userRequest.getAddress());
        user.setUniqueId(userRequest.getUniqueId());

        UserEntity savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setUserId(savedUser.getUserId());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setContactNumber(savedUser.getContactNumber());
        response.setAddress(savedUser.getAddress());
        response.setUniqueId(savedUser.getUniqueId());
        return response;
    }
}
