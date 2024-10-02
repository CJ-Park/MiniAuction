package com.example.miniauction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Long balance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransactionLog> transactionLogs = new ArrayList<>();

    public List<TransactionLog> getTransactionLogs() {
        return Collections.unmodifiableList(transactionLogs);
    }

    public void deposit(Long amount) {
        balance += amount;
    }

    public void withdraw(Long amount) {
        balance -= amount;
    }
}
