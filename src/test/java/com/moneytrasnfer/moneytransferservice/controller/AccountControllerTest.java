package com.moneytrasnfer.moneytransferservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.exception.GlobalExceptionHandler;
import com.moneytrasnfer.moneytransferservice.model.AccountRequest;
import com.moneytrasnfer.moneytransferservice.model.AccountResponse;
import com.moneytrasnfer.moneytransferservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AccountService accountService;

    @Test
    void createAccount_ReturnsCreated() throws Exception {

        Long userId = 1L;
        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");
        request.setAccountType("CHECKING");
        request.setInitialBalance(BigDecimal.valueOf(1000.00));

        AccountResponse response = new AccountResponse();
        response.setAccountNumber(101L);
        response.setCurrency("EUR");
        response.setAccountType("CHECKING");
        response.setBalance(BigDecimal.valueOf(1000.00));

        when(accountService.createAccount(anyLong(), any(AccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/{userId}/accounts", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value(101L))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void createAccount_ReturnsUnprocessableContent_WhenCurrencyAlreadyExists() throws Exception {

        Long userId = 1L;
        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");
        request.setAccountType("CHECKING");

        when(accountService.createAccount(anyLong(), any(AccountRequest.class)))
                .thenThrow(new FunctionalException(FunctionalErrorCode.ACCOUNT_ALREADY_EXISTS_FOR_CURRENCY));

        mockMvc.perform(post("/api/users/{userId}/accounts", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("User already has an account with this currency type"))
                .andExpect(jsonPath("$.['trace-id']").exists());
    }

    @Test
    void createAccount_ReturnsNotFound_WhenUserDoesNotExist() throws Exception {

        Long nonExistentUserId = 999L;
        AccountRequest request = new AccountRequest();
        request.setCurrency("EUR");
        request.setAccountType("CHECKING");
        request.setInitialBalance(BigDecimal.valueOf(100.00));

        when(accountService.createAccount(eq(nonExistentUserId), any(AccountRequest.class)))
                .thenThrow(new FunctionalException(FunctionalErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post("/api/users/{userId}/accounts", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.['trace-id']").exists());
    }
}
