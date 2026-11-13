package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.entity.AccountEntity;
import com.moneytrasnfer.moneytransferservice.entity.UserEntity;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.model.AccountRequest;
import com.moneytrasnfer.moneytransferservice.model.AccountResponse;
import com.moneytrasnfer.moneytransferservice.repository.AccountRepository;
import com.moneytrasnfer.moneytransferservice.repository.UserRepository;
import com.moneytrasnfer.moneytransferservice.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccount_Success() {

        Long userId = 1L;

        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");
        request.setAccountType("CHECKING");
        request.setInitialBalance(BigDecimal.valueOf(500.00));

        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setAccountNumber(101L);
        savedEntity.setCurrency("EUR");
        savedEntity.setAccountType("CHECKING");
        savedEntity.setBalance(BigDecimal.valueOf(500.00));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserEntity()));
        when(accountRepository.existsByUserUserIdAndCurrency(1L, "EUR")).thenReturn(false);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedEntity);

        AccountResponse response = accountService.createAccount(userId, request);

        assertNotNull(response);
        assertEquals(101L, response.getAccountNumber());
        assertEquals("EUR", response.getCurrency());
        verify(accountRepository, times(1)).existsByUserUserIdAndCurrency(1L, "EUR");
        verify(accountRepository, times(1)).save(any(AccountEntity.class));
    }

    @Test
    void createAccount_ThrowsFunctionalException_WhenCurrencyExistsForUser() {

        Long userId = 1L;
        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserEntity()));
        when(accountRepository.existsByUserUserIdAndCurrency(1L, "EUR")).thenReturn(true);

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            accountService.createAccount(userId, request);
        });

        assertEquals(FunctionalErrorCode.ACCOUNT_ALREADY_EXISTS_FOR_CURRENCY, exception.getErrorCode());
        assertEquals("User already has an account with this currency type", exception.getMessage());
        verify(accountRepository, times(1)).existsByUserUserIdAndCurrency(1L, "EUR");
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void createAccount_ThrowsFunctionalException_WhenUserDoesNotExist() {

        Long userId = 1L;
        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            accountService.createAccount(userId, request);
        });

        assertEquals(FunctionalErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertEquals("User not found", exception.getMessage());
        verify(accountRepository, never()).existsByUserUserIdAndCurrency(1L, "EUR");
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }
}
