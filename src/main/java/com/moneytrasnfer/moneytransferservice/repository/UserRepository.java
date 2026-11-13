package com.moneytrasnfer.moneytransferservice.repository;

import com.moneytrasnfer.moneytransferservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUniqueId(String uniqueId);
}
