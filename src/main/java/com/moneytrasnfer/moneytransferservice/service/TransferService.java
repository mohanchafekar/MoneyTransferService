package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.model.TransferHistoryResponse;
import com.moneytrasnfer.moneytransferservice.model.TransferRequest;
import com.moneytrasnfer.moneytransferservice.model.TransferResponse;

import java.util.List;

public interface TransferService {

    TransferResponse makeTransfer(TransferRequest transferRequest);
    List<TransferHistoryResponse> getTransferHistory(Long accountNumber);
}
