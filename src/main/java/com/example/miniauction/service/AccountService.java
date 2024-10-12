package com.example.miniauction.service;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.enums.TransactionType;

import java.util.List;
import java.util.concurrent.Future;

public interface AccountService {
    // 계좌 생성
    void generateAccount(Long userId);

    // 계좌 조회
    AccountDto getAccountBalance(Long userId);

    // 계좌 거래 내역 조회
    List<TransactionLogDto> getTransactionLogs(Long accountId);

    // 입금
    void deposit(AccountRequestDto dto, Long userId, TransactionType transactionType);

    // 출금
    Future<?> withdraw(AccountRequestDto dto, Long userId, TransactionType transactionType);

}
