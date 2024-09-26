package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.service.AccountService;

import java.util.List;

public class AccountServiceImpl implements AccountService {
    @Override
    public String generateAccountNumber() {
        return "";
    }

    @Override
    public AccountDto getAccountBalance(Long accountId) {
        return null;
    }

    @Override
    public List<TransactionLogDto> getTransactionLogs(Long accountId) {
        return List.of();
    }

    @Override
    public void deposit(Long accountId, Long amount) {

    }

    @Override
    public boolean withdraw(Long accountId, Long amount) {
        return false;
    }
}
