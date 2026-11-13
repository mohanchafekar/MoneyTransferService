package com.moneytrasnfer.moneytransferservice.service.impl;

import com.moneytrasnfer.moneytransferservice.entity.AccountEntity;
import com.moneytrasnfer.moneytransferservice.entity.UserEntity;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.model.AccountRequest;
import com.moneytrasnfer.moneytransferservice.model.AccountResponse;
import com.moneytrasnfer.moneytransferservice.repository.AccountRepository;
import com.moneytrasnfer.moneytransferservice.repository.UserRepository;
import com.moneytrasnfer.moneytransferservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, AccountRequest accountRequest) {
        //Check if user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new FunctionalException(FunctionalErrorCode.USER_NOT_FOUND));

        // Check the unique currency rule per user
        if (accountRepository.existsByUserUserIdAndCurrency(userId, accountRequest.getCurrency())) {
            log.warn("Attempt to create duplicate {} account for user ID: {}", accountRequest.getCurrency(), userId);
            throw new FunctionalException(FunctionalErrorCode.ACCOUNT_ALREADY_EXISTS_FOR_CURRENCY);
        }

        AccountEntity account = new AccountEntity();
        account.setUser(user);
        account.setAccountType(accountRequest.getAccountType());
        account.setCurrency(accountRequest.getCurrency());
        account.setBalance(accountRequest.getInitialBalance() != null ? accountRequest.getInitialBalance() : BigDecimal.ZERO);

        AccountEntity savedAccount = accountRepository.save(account);

        AccountResponse response = new AccountResponse();
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setUserId(user.getUserId());
        response.setAccountType(savedAccount.getAccountType());
        response.setCurrency(savedAccount.getCurrency());
        response.setBalance(savedAccount.getBalance());
        return response;
    }

    @Override
    public AccountResponse getAccount(Long accountNumber) {
        AccountEntity account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));

        AccountResponse response = new AccountResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setUserId(account.getUser().getUserId());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        return response;
    }
}
