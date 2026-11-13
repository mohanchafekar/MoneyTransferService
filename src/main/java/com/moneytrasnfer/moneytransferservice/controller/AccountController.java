package com.moneytrasnfer.moneytransferservice.controller;

import com.moneytrasnfer.moneytransferservice.api.AccountsApi;
import com.moneytrasnfer.moneytransferservice.model.AccountRequest;
import com.moneytrasnfer.moneytransferservice.model.AccountResponse;
import com.moneytrasnfer.moneytransferservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountResponse> createAccount(Long userId, AccountRequest accountRequest) {
        log.info("Create Account request received for User Id: {} and request is {}", userId, accountRequest);
        AccountResponse response = accountService.createAccount(userId, accountRequest);
        log.info("Account created for User Id: {}. Account details: {}", userId, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AccountResponse> getAccount(Long accountNumber) {
        log.info("Get Account request received for account Number: {}", accountNumber);
        AccountResponse response = accountService.getAccount(accountNumber);
        log.info("Account details for account number:{} is  {}", accountNumber, response);
        return ResponseEntity.ok(response);
    }
}
