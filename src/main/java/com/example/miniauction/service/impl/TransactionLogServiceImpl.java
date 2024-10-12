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

import static com.example.miniauction.util.MyLogger.log;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionLogServiceImpl implements TransactionLogService {
    private final TransactionLogRepository logRepository;

    @Async
    @Override
    public void createTransactionLog(Long amount, Long balance, TransactionType type, Account account) {
        TransactionLog log = TransactionLog.builder()
                .amount(amount)
                .account(account)
                .leftBalance(balance)
                .type(type)
                .build();


        logRepository.save(log);
        log("로그 저장 완료 - " + type.getDescription());
    }
}
