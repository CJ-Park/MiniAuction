package com.example.miniauction.service;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;

import java.util.List;

public interface AccountService {
    // 계좌 번호 생성
    String generateAccountNumber();

    // 계좌 조회
    AccountDto getAccountBalance(Long accountId);

    // 계좌 거래 내역 조회
    List<TransactionLogDto> getTransactionLogs(Long accountId);

    // 입금
    void deposit(Long accountId, Long amount);

    // 출금
    void withdraw(Long accountId, Long amount);

}
