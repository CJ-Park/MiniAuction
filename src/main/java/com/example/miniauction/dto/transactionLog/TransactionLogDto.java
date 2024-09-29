package com.example.miniauction.dto.transactionLog;

import com.example.miniauction.entity.TransactionLog;
import com.example.miniauction.enums.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TransactionLogDto {
    private Long id;
    private Long amount;
    private Long leftBalance;
    private TransactionType type;
    private LocalDateTime createdDate;

    public TransactionLogDto(TransactionLog log) {
        this.id = log.getId();
        this.amount = log.getAmount();
        this.leftBalance = log.getLeftBalance();
        this.type = log.getType();
        this.createdDate = log.getCreatedDate();
    }
}
