package com.example.miniauction.entity;

import com.example.miniauction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long leftBalance;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;
}
