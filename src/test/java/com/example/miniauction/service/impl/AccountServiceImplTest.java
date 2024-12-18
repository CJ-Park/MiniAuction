package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.TransactionLog;
import com.example.miniauction.repository.account.AccountRepository;
import com.example.miniauction.service.TransactionLogService;
import com.example.miniauction.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.example.miniauction.enums.TransactionType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @Mock
    private ExecutorService es;

    @Mock
    private TransactionLogService logService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private List<TransactionLog> transactionLogs;

    @BeforeEach
    public void setUp() throws Exception {
        transactionLogs = List.of(new TransactionLog(1L, 500L, 1000L,
                testAccount, DEPOSIT));
        testAccount = new Account(1L, "1234-5678", 1000L, transactionLogs);
    }

    @Test
    public void 계좌의_거래내역을_조회한다() throws Exception {
        //given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));

        //when
        List<TransactionLogDto> logs = accountService.getTransactionLogs(1L);

        //then
        assertThat(logs).isNotNull();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getType()).isEqualTo(DEPOSIT);
        assertThat(logs.getFirst().getAmount()).isEqualTo(500L);
    }

    @Test
    public void 계좌의_거래내역_조회_중_인터럽트_발생() throws Exception {
        //given
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                accountService.getTransactionLogs(1L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        //when
        t1.start();
        t1.interrupt();

        //then
        assertTrue(t1.isInterrupted());
    }

    @Test
    public void 계좌_잔고를_조회한다() throws Exception {
        //given
        when(userService.getUserAccount(any())).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        //when
        AccountDto accountDto = accountService.getAccountBalance(1L);

        //then
        assertThat(accountDto).isOfAnyClassIn(AccountDto.class);
        assertThat(accountDto.getBalance()).isEqualTo(1000L);
    }

    @Test
    public void 출금_후_계좌_잔고를_조회한다() throws Exception {
        //given
        when(userService.getUserAccount(any())).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return CompletableFuture.completedFuture(null); // 반환값이 필요 없으므로 null
        });
        AccountRequestDto dto = new AccountRequestDto(100L);
        CountDownLatch latch = new CountDownLatch(3);

        //when
        for (int i = 0; i < 4; i++) {
            Future<?> future;
            if (i == 2) {
                future = es.submit(() -> {
                    AccountDto accountDto = accountService.getAccountBalance(1L);
                    assertThat(accountDto.getBalance()).isEqualTo(800L);
                });
            } else {
                 future = es.submit(() -> {
                    accountService.withdraw(dto, 1L, WITHDRAWAL);
                    latch.countDown();
                });
            }
            future.get();
        }

        // lock 으로 인해 출금 완료 후 잔고 조회됨
        AccountDto accountDto = accountService.getAccountBalance(1L);
        assertThat(accountDto.getBalance()).isEqualTo(700L);

        latch.await();
        es.close();

        //then
        assertThat(accountDto).isOfAnyClassIn(AccountDto.class);
        assertThat(accountDto.getBalance()).isEqualTo(700L);
    }

    @Test
    public void 계좌를_생성한다() throws Exception {
        // given
        String accountNumber = "1234-5678";
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(0L)
                .build();

        // 계좌 번호 중복 체크와 저장 과정 모킹
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountRepository.existsAccountByAccountNumber(anyString())).thenReturn(false);

        // when
        accountService.generateAccount(1L); // 계좌 생성

        // then
        // 계좌가 저장되는지 확인
        verify(accountRepository, times(1)).save(any(Account.class));
        // 계좌가 사용자에게 연결되는지 확인
        verify(userService, times(1)).connectAccount(any(Account.class), any());
    }

    @Test
    public void 계좌에_500원을_입금한다() throws Exception {
        //given
        when(userService.getUserAccount(any())).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return CompletableFuture.completedFuture(null); // 반환값이 필요 없으므로 null
        });
        AccountRequestDto dto = new AccountRequestDto(500L);

        //when
        es.submit(() -> {
            accountService.deposit(dto, 1L, DEPOSIT);
        });

        //then
        assertThat(testAccount.getBalance()).isEqualTo(1500L);
    }

    @Test
    public void 계좌에_5번_동시에_입금한다() throws Exception {
        //given
        when(userService.getUserAccount(any())).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return CompletableFuture.completedFuture(null); // 반환값이 필요 없으므로 null
        });
        AccountRequestDto dto = new AccountRequestDto(500L);
        CountDownLatch latch = new CountDownLatch(5); // 입금 작업 개수만큼 카운트다운 설정

        //when
        for (int i = 0; i < 5; i++) {
            es.submit(() -> {
                accountService.deposit(dto, 1L, DEPOSIT);
                latch.countDown();
            });
        }

        latch.await();
        es.close();

        //then
        assertEquals(3500L, testAccount.getBalance());
    }

    @Test
    public void 계좌에서_100원씩_5번_동시에_출금한다() throws Exception {
        // given
        when(userService.getUserAccount(any())).thenReturn(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return CompletableFuture.completedFuture(null); // 반환값이 필요 없으므로 null
        });
        AccountRequestDto dto = new AccountRequestDto(100L);
        CountDownLatch latch = new CountDownLatch(5);

        // when
        for (int i = 0; i < 5; i++) {
            es.submit(() -> {
                accountService.withdraw(dto, 1L, WITHDRAWAL);
                latch.countDown();
            });
        }

        es.close();

        // 최종 잔액이 500L인지 확인 (100씩 5번 출금 -> 초기 잔액 1000에서 500으로)
        assertThat(testAccount.getBalance()).isEqualTo(500L);
    }

    @Test
    public void 출금시_잔고가_부족하면_예외가_발생한다() throws Exception {
        // given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return CompletableFuture.completedFuture(null); // 반환값이 필요 없으므로 null
        });
        AccountRequestDto dto = new AccountRequestDto(5000L);

        // when
        // then
        assertThrows(RuntimeException.class, () -> accountService.withdraw(dto, 1L, WITHDRAWAL));
    }
}