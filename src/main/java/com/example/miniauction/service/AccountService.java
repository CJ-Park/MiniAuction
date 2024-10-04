package com.example.miniauction.service;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.TransactionType;

import java.util.List;

public interface AccountService {
    // 계좌 생성
    void generateAccount(User user);

    // 계좌 조회
    AccountDto getAccountBalance(Long accountId);

    // 계좌 거래 내역 조회
    List<TransactionLogDto> getTransactionLogs(Long accountId);

    // 입금
    void deposit(Long accountId, Long amount, TransactionType transactionType);

    // 출금
    void withdraw(Long accountId, Long amount, TransactionType transactionType);

}
