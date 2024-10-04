package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.TransactionType;
import com.example.miniauction.repository.account.AccountRepository;
import com.example.miniauction.service.AccountService;
import com.example.miniauction.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.miniauction.util.ThreadUtils.sleep;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionLogService logService;
    private final Lock lock = new ReentrantLock();
    private final ExecutorService es;
    private final Random random = new Random();

    // 사용자가 많아지면 매우 비효율적인 방법 => 추후 사용자 많아지면 데이터베이스 시퀀스 필요
    private String generateAccountNumber() {
        return String.format("%04d-%04d",
                random.nextInt(10000), random.nextInt(10000));
    }

    @Override
    @Transactional
    public void generateAccount(User user) {
        lock.lock();

        try {
            String accountNumber;
            do {
                accountNumber = generateAccountNumber();
            } while (accountRepository.existsAccountByAccountNumber(accountNumber));

            Account account = Account.builder()
                    .accountNumber(accountNumber)
                    .balance(0L)
                    .build();

            Account saveAccount = accountRepository.save(account);
            user.connectAccount(saveAccount);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AccountDto getAccountBalance(Long accountId) {
        lock.lock();

        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            return new AccountDto(account);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<TransactionLogDto> getTransactionLogs(Long accountId) {
        Future<List<TransactionLogDto>> future = es.submit(() -> {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            return account.getTransactionLogs().stream().map(TransactionLogDto::new).toList();
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("로그 조회 중 예외 발생, 다시 시도하세요", e);
        }
    }

    @Override
    @Transactional
    public void deposit(Long accountId, Long amount, TransactionType transactionType) {
        es.submit(() -> {
            lock.lock();
            Account account;
            try {
                 account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));

                sleep(1000); // 입금 1초 걸린다고 가정
                account.deposit(amount);
            } finally {
                lock.unlock();
            }

//            logService.createTransactionLog(amount, transactionType, account);
        });
    }

    @Override
    @Transactional
    public void withdraw(Long accountId, Long amount, TransactionType transactionType) {
        es.submit(() -> {
            lock.lock();
            Account account;
            try {
                 account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));

                if (account.getBalance() < amount) {
                    throw new RuntimeException("잔고가 부족합니다.");
                } else {
                    sleep(1000); // 출금 1초 걸린다고 가정
                    account.withdraw(amount);
                }
            } finally {
                lock.unlock();
            }

//            logService.createTransactionLog(amount, transactionType, account);
        });
    }
}
