package com.moneytrasnfer.moneytransferservice.repository;

import com.moneytrasnfer.moneytransferservice.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    // Checks if an account exists for this user ID and currency code
    boolean existsByUserUserIdAndCurrency(Long userId, String currency);
}
