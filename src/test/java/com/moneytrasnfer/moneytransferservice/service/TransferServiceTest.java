package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.entity.AccountEntity;
import com.moneytrasnfer.moneytransferservice.entity.TransferEntity;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.FunctionalException;
import com.moneytrasnfer.moneytransferservice.exception.TechnicalErrorCode;
import com.moneytrasnfer.moneytransferservice.exception.TechnicalException;
import com.moneytrasnfer.moneytransferservice.model.TransferHistoryResponse;
import com.moneytrasnfer.moneytransferservice.model.TransferRequest;
import com.moneytrasnfer.moneytransferservice.model.TransferResponse;
import com.moneytrasnfer.moneytransferservice.repository.AccountRepository;
import com.moneytrasnfer.moneytransferservice.repository.TransferRepository;
import com.moneytrasnfer.moneytransferservice.service.impl.AuditServiceImpl;
import com.moneytrasnfer.moneytransferservice.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuditServiceImpl auditService;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Test
    void executeTransfer_ThrowsFunctionalException_WhenMultipleRulesViolated() {

        Long sourceAccNum = 101L;
        Long targetAccNum = 102L;
        BigDecimal transferAmount = BigDecimal.valueOf(1500.00); // Exceeds single transfer limit (1000)

        AccountEntity sourceAccount = new AccountEntity();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(BigDecimal.valueOf(500.00)); // Insufficient balance (< 1500)
        sourceAccount.setCurrency("EUR");

        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setAccountNumber(targetAccNum);
        targetAccount.setCurrency("USD"); // Cross-currency check (amount > 500)

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccNum);
        request.setTargetAccountNumber(targetAccNum);
        request.setAmount(transferAmount);

        when(accountRepository.findById(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccNum)).thenReturn(Optional.of(targetAccount));

        when(transferRepository.findSuccessfulTransfersSince(eq(sourceAccNum), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        doNothing().when(auditService).saveFailedTransfer(any(TransferEntity.class));

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            transferService.makeTransfer(request);
        });

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getErrorCode().getHttpStatus());
        assertTrue(exception.getMessage().contains("Single transfer limit"));
        assertTrue(exception.getMessage().contains("Insufficient balance in source account"));
        assertTrue(exception.getMessage().contains("Cross-currency limit"));

        verify(auditService, times(1)).saveFailedTransfer(any(TransferEntity.class));
    }

    @Test
    void makeTransfer_ThrowsFunctionalException_WhenDailyLimitExceeded() {

        Long sourceAccNum = 101L;
        Long targetAccNum = 102L;
        BigDecimal transferAmount = BigDecimal.valueOf(800.00);

        AccountEntity sourceAccount = new AccountEntity();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(BigDecimal.valueOf(5000.00)); // Sufficient balance
        sourceAccount.setCurrency("EUR");

        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setAccountNumber(targetAccNum);
        targetAccount.setCurrency("EUR"); // Same currency, no cross-currency limit

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccNum);
        request.setTargetAccountNumber(targetAccNum);
        request.setAmount(transferAmount);

        when(accountRepository.findById(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccNum)).thenReturn(Optional.of(targetAccount));

        // Mock existing successful transfers today totaling 2000.00
        // (2000 + 800 = 2800, which exceeds the 2500 daily limit)
        TransferEntity existingTransfer = new TransferEntity();
        existingTransfer.setAmountSent(BigDecimal.valueOf(2000.00));

        when(transferRepository.findSuccessfulTransfersSince(eq(sourceAccNum), any(LocalDateTime.class)))
                .thenReturn(List.of(existingTransfer));
        doNothing().when(auditService).saveFailedTransfer(any(TransferEntity.class));

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            transferService.makeTransfer(request);
        });

        assertTrue(exception.getMessage().contains("Daily limit"));

        verify(auditService, times(1)).saveFailedTransfer(any(TransferEntity.class));
    }

    @Test
    void makeTransfer_ThrowsFunctionalException_WhenSameAccountTransfer() {

        Long accountNumber = 101L;
        BigDecimal transferAmount = BigDecimal.valueOf(100.00);

        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setCurrency("EUR");

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(accountNumber);
        request.setTargetAccountNumber(accountNumber); // Same account
        request.setAmount(transferAmount);

        when(accountRepository.findById(accountNumber)).thenReturn(Optional.of(account));
        doNothing().when(auditService).saveFailedTransfer(any(TransferEntity.class));

        FunctionalException exception = assertThrows(FunctionalException.class, () -> {
            transferService.makeTransfer(request);
        });

        assertTrue(exception.getMessage().contains(FunctionalErrorCode.SAME_ACCOUNT_TRANSFER.getDefaultMessage()));

        verify(auditService, times(1)).saveFailedTransfer(any(TransferEntity.class));
    }

    @Test
    void makeTransfer_Succeeds_WhenAllRulesPass() {

        Long sourceAccNum = 101L;
        Long targetAccNum = 102L;
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);

        AccountEntity sourceAccount = new AccountEntity();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(BigDecimal.valueOf(1000.00));
        sourceAccount.setCurrency("EUR");

        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setAccountNumber(targetAccNum);
        targetAccount.setBalance(BigDecimal.valueOf(500.00));
        sourceAccount.setCurrency("EUR");
        targetAccount.setCurrency("EUR"); // Same currency

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccNum);
        request.setTargetAccountNumber(targetAccNum);
        request.setAmount(transferAmount);

        when(accountRepository.findById(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccNum)).thenReturn(Optional.of(targetAccount));

        // No daily limits breached
        when(transferRepository.findSuccessfulTransfersSince(eq(sourceAccNum), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        transferService.makeTransfer(request);

        assertEquals(0, sourceAccount.getBalance().compareTo(BigDecimal.valueOf(800.00)));
        assertEquals(0, targetAccount.getBalance().compareTo(BigDecimal.valueOf(700.00)));

        // Verify account updates are saved
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(targetAccount);

        // Verify audit trail records a successful transfer status
        verify(transferRepository, times(1)).save(argThat(transfer ->
                "SUCCESS".equals(transfer.getStatus()) &&
                        transfer.getAmountSent().compareTo(transferAmount) == 0
        ));
    }

    @Test
    void makeTransfer_Succeeds_WithCrossCurrencyConversion() {

        Long sourceAccNum = 101L;
        Long targetAccNum = 102L;
        BigDecimal transferAmount = BigDecimal.valueOf(100.00); // 100 EUR

        AccountEntity sourceAccount = new AccountEntity();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(BigDecimal.valueOf(500.00));
        sourceAccount.setCurrency("EUR");

        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setAccountNumber(targetAccNum);
        targetAccount.setBalance(BigDecimal.valueOf(200.00));
        targetAccount.setCurrency("USD");

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccNum);
        request.setTargetAccountNumber(targetAccNum);
        request.setAmount(transferAmount);

        when(accountRepository.findById(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccNum)).thenReturn(Optional.of(targetAccount));

        // No daily limits breached
        when(transferRepository.findSuccessfulTransfersSince(eq(sourceAccNum), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Mock the external Frankfurter API response via RestTemplate
        // Example: 1 EUR = 1.10 USD
        Map<String, Object> mockApiResponse = new HashMap<>();
        Map<String, Number> mockRates = new HashMap<>();
        mockRates.put("USD", 1.10);
        mockApiResponse.put("rates", mockRates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(mockApiResponse);

        TransferResponse response = transferService.makeTransfer(request);

        // Source balance deducted by 100.00 EUR (500 - 100 = 400)
        assertEquals(0, sourceAccount.getBalance().compareTo(BigDecimal.valueOf(400.00)));

        // Target balance credited by converted amount (100 EUR * 1.10 = 110.00 USD -> 200 + 110 =        //Check that transaction rate is not 1
        assertEquals(0, targetAccount.getBalance().compareTo(BigDecimal.valueOf(310.00)));

        // Assert that the exchange rate for a cross-currency transfer is not 1.00
        assertNotEquals(0, response.getExchangeRate().compareTo(BigDecimal.valueOf(1.00)));

        // Verify repository and audit updates
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(targetAccount);
    }

    @Test
    void makeTransfer_ThrowsTechnicalException_WhenExchangeRateApiFails() {

        Long sourceAccNum = 101L;
        Long targetAccNum = 102L;
        BigDecimal transferAmount = BigDecimal.valueOf(100.00); // Cross-currency transfer

        AccountEntity sourceAccount = new AccountEntity();
        sourceAccount.setAccountNumber(sourceAccNum);
        sourceAccount.setBalance(BigDecimal.valueOf(500.00));
        sourceAccount.setCurrency("EUR");

        AccountEntity targetAccount = new AccountEntity();
        targetAccount.setAccountNumber(targetAccNum);
        targetAccount.setBalance(BigDecimal.valueOf(200.00));
        targetAccount.setCurrency("GBP"); // Different currency forces external API call

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber(sourceAccNum);
        request.setTargetAccountNumber(targetAccNum);
        request.setAmount(transferAmount);

        when(accountRepository.findById(sourceAccNum)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(targetAccNum)).thenReturn(Optional.of(targetAccount));

        when(transferRepository.findSuccessfulTransfersSince(eq(sourceAccNum), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Mock RestTemplate throwing a network/connection exception when hitting Frankfurter API
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("Connection timeout"));

        TechnicalException exception = assertThrows(TechnicalException.class, () -> {
            transferService.makeTransfer(request);
        });

        // Verify it maps to your technical error code and service unavailable/server error status
        assertEquals(TechnicalErrorCode.EXCHANGE_RATE_SERVICE_FAILURE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to fetch exchange rate"));

        // Ensure no account balances were modified and no success audit was recorded
        verify(accountRepository, never()).save(any());
        verify(transferRepository, never()).save(argThat(transfer -> "SUCCESS".equals(transfer.getStatus())));
    }

    @Test
    void getTransferHistory_Succeeds_WhenTransfersExist() {
        Long accountNumber = 101L;

        TransferEntity transfer = new TransferEntity();
        transfer.setTransferId(1L);
        transfer.setSourceAccountNumber(101L);
        transfer.setTargetAccountNumber(102L);
        transfer.setAmountSent(BigDecimal.valueOf(100.00));
        transfer.setAmountReceived(BigDecimal.valueOf(100.00));
        transfer.setExchangeRate(BigDecimal.ONE);
        transfer.setStatus("SUCCESS");
        transfer.setDateOfTransfer(LocalDateTime.now());

        when(transferRepository.findBySourceAccountNumberOrTargetAccountNumber(accountNumber, accountNumber))
                .thenReturn(List.of(transfer));

        List<TransferHistoryResponse> responseList = transferService.getTransferHistory(accountNumber);

        assertNotNull(responseList);
        assertEquals(1, responseList.size());

        TransferHistoryResponse response = responseList.getFirst();
        assertEquals(1L, response.getTransferId());
        assertEquals(101L, response.getSourceAccountNumber());
        assertEquals(102L, response.getTargetAccountNumber());
        assertEquals(0, response.getAmountSent().compareTo(BigDecimal.valueOf(100.00)));
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getDateOfTransfer());
    }

    @Test
    void getTransferHistory_ReturnsEmptyList_WhenNoTransfersExist() {

        Long accountNumber = 999L;

        when(transferRepository.findBySourceAccountNumberOrTargetAccountNumber(accountNumber, accountNumber))
                .thenReturn(Collections.emptyList());

        List<TransferHistoryResponse> responseList = transferService.getTransferHistory(accountNumber);

        // Assert
        assertNotNull(responseList);
        assertTrue(responseList.isEmpty());
    }
}
