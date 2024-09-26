package com.example.miniauction.service;

import com.example.miniauction.entity.Account;
import com.example.miniauction.enums.TransactionType;

public interface TransactionLogService {
    // 거래내역 로그 생성
    void createTransactionLog(Long amount, TransactionType type, Account account);
}
