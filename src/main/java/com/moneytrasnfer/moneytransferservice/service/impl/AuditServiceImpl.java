package com.moneytrasnfer.moneytransferservice.service.impl;

import com.moneytrasnfer.moneytransferservice.entity.TransferEntity;
import com.moneytrasnfer.moneytransferservice.repository.TransferRepository;
import com.moneytrasnfer.moneytransferservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final TransferRepository transferRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransfer(TransferEntity failedTransfer) {
        transferRepository.save(failedTransfer);
    }
}
