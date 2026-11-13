package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.model.AccountRequest;
import com.moneytrasnfer.moneytransferservice.model.AccountResponse;

public interface AccountService {

    AccountResponse createAccount(Long userId, AccountRequest accountRequest);
    AccountResponse getAccount(Long accountNumber);
}
