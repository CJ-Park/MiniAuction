package com.example.miniauction.service.impl;

import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.TransactionLog;
import com.example.miniauction.enums.TransactionType;
import com.example.miniauction.repository.tansactionLog.TransactionLogRepository;
import com.example.miniauction.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionLogServiceImpl implements TransactionLogService {
    private final TransactionLogRepository logRepository;

    @Async
    @Override
    public void createTransactionLog(Long amount, TransactionType type, Account account) {
        Long left = account.getBalance();
        TransactionLog log = TransactionLog.builder()
                .amount(amount)
                .account(account)
                .leftBalance(left)
                .type(type)
                .build();

        logRepository.save(log);
    }
}
