package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.model.UserRequest;
import com.moneytrasnfer.moneytransferservice.model.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);
}
