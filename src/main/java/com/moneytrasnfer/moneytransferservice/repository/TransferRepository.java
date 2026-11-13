package com.moneytrasnfer.moneytransferservice.repository;

import com.moneytrasnfer.moneytransferservice.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    // Retrieves all successful transfers sent by an account since a specific timestamp (for daily limit validation)
    @Query("SELECT t FROM TransferEntity t WHERE t.sourceAccountNumber = :accountNumber AND t.status = 'SUCCESS' AND t.dateOfTransfer >= :startDateTime")
    List<TransferEntity> findSuccessfulTransfersSince(
            @Param("accountNumber") Long accountNumber,
            @Param("startDateTime") LocalDateTime startDateTime
    );

    // Retrieves transfer history where the account is either the sender or the receiver
    List<TransferEntity> findBySourceAccountNumberOrTargetAccountNumber(Long sourceAccountNumber, Long targetAccountNumber);
}
