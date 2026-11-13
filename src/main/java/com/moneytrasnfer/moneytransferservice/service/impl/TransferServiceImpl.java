package com.moneytrasnfer.moneytransferservice.service.impl;

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
import com.moneytrasnfer.moneytransferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public TransferResponse makeTransfer(TransferRequest request) {
        AccountEntity sourceAccount = accountRepository.findById(request.getSourceAccountNumber())
                .orElseThrow(() -> new FunctionalException(FunctionalErrorCode.SOURCE_ACCOUNT_NOT_FOUND));
        AccountEntity targetAccount = accountRepository.findById(request.getTargetAccountNumber())
                .orElseThrow(() -> new FunctionalException(FunctionalErrorCode.TARGET_ACCOUNT_NOT_FOUND));

        BigDecimal amount = request.getAmount();
        List<String> violatedRules = new ArrayList<>();

        // 1. Basic Validation
        if (sourceAccount.getAccountNumber().equals(targetAccount.getAccountNumber())) {
            violatedRules.add(FunctionalErrorCode.SAME_ACCOUNT_TRANSFER.getDefaultMessage());
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            violatedRules.add(FunctionalErrorCode.INVALID_AMOUNT.getDefaultMessage());
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            violatedRules.add(FunctionalErrorCode.INSUFFICIENT_BALANCE.getDefaultMessage());
        }

        // 2. Rule Validations
        // Single transfer limit: > 1000
        if (amount.compareTo(new BigDecimal("1000.00")) > 0) {
            violatedRules.add(FunctionalErrorCode.SINGLE_TRANSFER_LIMIT_EXCEEDED.getDefaultMessage());
        }

        // Daily limit: Total successful transfers today including this one > 2500
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        BigDecimal todayTransfersSum = transferRepository.findSuccessfulTransfersSince(
                sourceAccount.getAccountNumber(), startOfDay
        ).stream().map(TransferEntity::getAmountSent).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTransfersSum.add(amount).compareTo(new BigDecimal("2500.00")) > 0) {
            violatedRules.add(FunctionalErrorCode.DAILY_LIMIT_EXCEEDED.getDefaultMessage());
        }

        // Cross-currency limit: different currencies and amount > 500
        boolean isCrossCurrency = !sourceAccount.getCurrency().equalsIgnoreCase(targetAccount.getCurrency());
        if (isCrossCurrency && amount.compareTo(new BigDecimal("500.00")) > 0) {
            violatedRules.add(FunctionalErrorCode.CROSS_CURRENCY_LIMIT_EXCEEDED.getDefaultMessage());
        }

        // Handle Rejection
        if (!violatedRules.isEmpty()) {
            String failureMessage = "Rules violated: " + String.join(", ", violatedRules);

            // Save failed transfer to history
            TransferEntity failedTransfer = new TransferEntity();
            failedTransfer.setSourceAccountNumber(sourceAccount.getAccountNumber());
            failedTransfer.setTargetAccountNumber(targetAccount.getAccountNumber());
            failedTransfer.setAmountSent(amount);
            failedTransfer.setAmountSentCurrency(sourceAccount.getCurrency());
            failedTransfer.setAmountReceived(BigDecimal.ZERO);
            failedTransfer.setAmountReceivedCurrency(targetAccount.getCurrency());
            failedTransfer.setStatus("REJECTED");
            failedTransfer.setFailureReason(failureMessage);
            transferRepository.save(failedTransfer);

            throw new FunctionalException(FunctionalErrorCode.TRANSFER_RULE_VIOLATION, failureMessage);
        }

        // 3. Currency Conversion & Execution
        BigDecimal exchangeRate = BigDecimal.ONE;
        BigDecimal amountReceived = amount;

        if (isCrossCurrency) {
            exchangeRate = fetchExchangeRate(sourceAccount.getCurrency(), targetAccount.getCurrency());
            amountReceived = amount.multiply(exchangeRate);
        }

        // Update balances
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amountReceived));
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Save successful transfer history
        TransferEntity successTransfer = new TransferEntity();
        successTransfer.setSourceAccountNumber(sourceAccount.getAccountNumber());
        successTransfer.setTargetAccountNumber(targetAccount.getAccountNumber());
        successTransfer.setAmountSent(amount);
        successTransfer.setAmountSentCurrency(sourceAccount.getCurrency());
        successTransfer.setAmountReceived(amountReceived);
        successTransfer.setAmountReceivedCurrency(targetAccount.getCurrency());
        successTransfer.setExchangeRate(exchangeRate);
        successTransfer.setStatus("SUCCESS");
        transferRepository.save(successTransfer);

        // Map Response
        TransferResponse response = new TransferResponse();
        response.setTransferId(successTransfer.getTransferId());
        response.setSourceAccountNumber(sourceAccount.getAccountNumber());
        response.setTargetAccountNumber(targetAccount.getAccountNumber());
        response.setAmountSent(amount);
        response.setAmountReceived(amountReceived);
        response.setExchangeRate(exchangeRate);
        response.setStatus("SUCCESS");
        response.setDateOfTransfer(successTransfer.getDateOfTransfer().atOffset(java.time.ZoneOffset.UTC));
        return response;
    }

    @Override
    public List<TransferHistoryResponse> getTransferHistory(Long accountNumber) {
        List<TransferEntity> transfers = transferRepository.findBySourceAccountNumberOrTargetAccountNumber(accountNumber, accountNumber);

        return transfers.stream().map(t -> {
            TransferHistoryResponse dto = new TransferHistoryResponse();
            dto.setTransferId(t.getTransferId());
            dto.setSourceAccountNumber(t.getSourceAccountNumber());
            dto.setTargetAccountNumber(t.getTargetAccountNumber());
            dto.setAmountSent(t.getAmountSent());
            dto.setAmountReceived(t.getAmountReceived());
            dto.setExchangeRate(t.getExchangeRate());
            dto.setStatus(t.getStatus());
            dto.setFailureReason(JsonNullable.of(t.getFailureReason()));
            if (t.getDateOfTransfer() != null) {
                dto.setDateOfTransfer(t.getDateOfTransfer().atOffset(java.time.ZoneOffset.UTC));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private BigDecimal fetchExchangeRate(String baseCurrency, String targetCurrency) {
        try {
            String url = String.format("https://api.frankfurter.app/latest?from=%s&to=%s", baseCurrency, targetCurrency);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("rates")) {
                Map<String, Number> rates = (Map<String, Number>) response.get("rates");
                if (rates.containsKey(targetCurrency)) {
                    return new BigDecimal(rates.get(targetCurrency).toString());
                }
            }
            // If rates container is missing the target currency
            throw new TechnicalException(TechnicalErrorCode.EXCHANGE_RATE_SERVICE_FAILURE,
                    "Exchange rate not found for target currency: " + targetCurrency);
        } catch (Exception e) {
            throw new TechnicalException(TechnicalErrorCode.EXCHANGE_RATE_SERVICE_FAILURE,
                    "Failed to fetch exchange rate: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
