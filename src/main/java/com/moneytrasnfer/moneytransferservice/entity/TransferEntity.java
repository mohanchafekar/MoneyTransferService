package com.moneytrasnfer.moneytransferservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    @Column(nullable = false)
    private Long sourceAccountNumber;

    @Column(nullable = false)
    private Long targetAccountNumber;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal amountSent;

    @Column(name = "amount_sent_currency", nullable = false)
    private String amountSentCurrency;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal amountReceived;

    @Column(name = "amount_received_currency", nullable = false)
    private String amountReceivedCurrency;

    @Column(precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private String status; // SUCCESS or REJECTED

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateOfTransfer = LocalDateTime.now();
}
