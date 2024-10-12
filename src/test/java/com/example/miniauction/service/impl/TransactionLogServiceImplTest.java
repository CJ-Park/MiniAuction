package com.example.miniauction.service.impl;

import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.TransactionLog;
import com.example.miniauction.repository.tansactionLog.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.miniauction.enums.TransactionType.DEPOSIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionLogServiceImplTest {
    @Mock
    TransactionLogRepository logRepository;

    @InjectMocks
    TransactionLogServiceImpl logService;

    private Account testAccount;
    private List<TransactionLog> transactionLogs;

    @BeforeEach
    void setUp() {
        transactionLogs = List.of(new TransactionLog(1L, 500L, 1000L,
                testAccount, DEPOSIT));
        testAccount = new Account(1L, "1234-5678", 1000L, transactionLogs);
    }

    @Test
    public void 로그를_생성할_수_있다() throws Exception {
        //given
        final Long amount = 1000L;

        //when
        logService.createTransactionLog(amount, testAccount.getBalance() + amount, DEPOSIT, testAccount);

        //then
        verify(logRepository, times(1)).save(any());
    }
}