package com.moneytrasnfer.moneytransferservice.controller;

import com.moneytrasnfer.moneytransferservice.api.UsersApi;
import com.moneytrasnfer.moneytransferservice.model.UserRequest;
import com.moneytrasnfer.moneytransferservice.model.UserResponse;
import com.moneytrasnfer.moneytransferservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(@Valid UserRequest userRequest) {
        log.info("Create user request received: {}", userRequest);
        UserResponse response = userService.createUser(userRequest);
        log.info("Create user response: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
