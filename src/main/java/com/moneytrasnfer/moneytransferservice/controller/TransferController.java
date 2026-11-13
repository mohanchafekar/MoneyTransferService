package com.moneytrasnfer.moneytransferservice.controller;

import com.moneytrasnfer.moneytransferservice.api.TransfersApi;
import com.moneytrasnfer.moneytransferservice.model.TransferHistoryResponse;
import com.moneytrasnfer.moneytransferservice.model.TransferRequest;
import com.moneytrasnfer.moneytransferservice.model.TransferResponse;
import com.moneytrasnfer.moneytransferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransferController implements TransfersApi {

    private final TransferService transferService;

    @Override
    public ResponseEntity<TransferResponse> makeTransfer(TransferRequest transferRequest) {
        log.info("Request received for the transfer: {}", transferRequest);
        TransferResponse response = transferService.makeTransfer(transferRequest);
        log.info("Response for Transfer: {}", response);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<TransferHistoryResponse>> getTransferHistory(Long accountNumber) {
        log.info("Get All transfer history for account: {}", accountNumber);
        List<TransferHistoryResponse> history = transferService.getTransferHistory(accountNumber);
        return ResponseEntity.ok(history);
    }
}
