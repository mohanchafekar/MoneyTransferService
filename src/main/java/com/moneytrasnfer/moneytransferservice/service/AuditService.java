package com.moneytrasnfer.moneytransferservice.service;

import com.moneytrasnfer.moneytransferservice.entity.TransferEntity;

public interface AuditService {
    public void saveFailedTransfer(TransferEntity failedTransfer);
}
