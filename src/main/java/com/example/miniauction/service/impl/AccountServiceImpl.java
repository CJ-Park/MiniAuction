package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.enums.TransactionType;
import com.example.miniauction.repository.account.AccountRepository;
import com.example.miniauction.service.AccountService;
import com.example.miniauction.service.TransactionLogService;
import com.example.miniauction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.miniauction.util.MyLogger.log;
import static com.example.miniauction.util.ThreadUtils.sleep;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;
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
    public void generateAccount(Long userId) {
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
            userService.connectAccount(saveAccount, userId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public AccountDto getAccountBalance(Long userId) {
        Long accountId = userService.getUserAccount(userId);

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return account.getTransactionLogs().stream().map(TransactionLogDto::new).toList();
    }

    @Override
    @Transactional
    public void deposit(AccountRequestDto dto, Long userId, TransactionType transactionType) {
        Long accountId = userService.getUserAccount(userId);

        es.submit(() -> {
            lock.lock();
            Account account;
            try {
                account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));

                log("입금 전 잔고 - " + account.getBalance() + " / 유저 아이디 - " + userId);
                account.deposit(dto.getAmount());

                log("입금중..");
                sleep(1000); // 입금 1초 걸린다고 가정

                accountRepository.save(account);
                log("입금 완료! / 잔고 - " + account.getBalance() + " / 유저 아이디 - " + userId);

                // 입금 완료 이벤트 발생 (사용자에게 알림 / 푸시 이벤트 등)

                // 입출금 로그 생성
                logService.createTransactionLog(dto.getAmount(), account.getBalance(), transactionType, account);
            } catch (Exception e) {
                // 입금 실패 이벤트 발생
                log("입금에 실패했습니다. 예외 - " + e.getMessage());
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        });
    }

    // 입찰 시 동기화 및 예외 처리를 위해 Future<?> 타입 반환
    @Override
    @Transactional
    public Future<?> withdraw(AccountRequestDto dto, Long userId, TransactionType transactionType) {
        Long accountId = userService.getUserAccount(userId);

        return es.submit(() -> {
            lock.lock();
            Account account;
            try {
                account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found"));

                if (account.getBalance() < dto.getAmount()) {
                    throw new RuntimeException("잔고가 부족합니다.");
                } else {
                    log("출금 전 잔고 - " + account.getBalance() + " / 유저 아이디 - " + userId);
                    account.withdraw(dto.getAmount());

                    log("출금중..");
                    sleep(1000); // 출금 1초 걸린다고 가정

                    accountRepository.save(account);
                    log("출금 완료! / 잔고 - " + account.getBalance() + " / 유저 아이디 - " + userId);

                    // 출금 완료 이벤트 발생 (사용자에게 알림 / 푸시 이벤트 등)

                    // 입출금 로그 생성
                    logService.createTransactionLog(dto.getAmount(), account.getBalance(), transactionType, account);
                }
            } catch (Exception e) {
                // 출금 실패 이벤트 발생
                log("출금에 실패했습니다. 예외 - " + e.getMessage());
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        });
    }
}
