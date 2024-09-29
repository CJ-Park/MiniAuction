package com.example.miniauction.service.impl;

import com.example.miniauction.dto.transactionLog.TransactionLogDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.TransactionLog;
import com.example.miniauction.enums.TransactionType;
import com.example.miniauction.repository.account.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.concurrent.FutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.example.miniauction.enums.TransactionType.*;
import static com.example.miniauction.util.ThreadUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExecutorService es;

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
        Future<List<TransactionLogDto>> mockFuture = mock(Future.class);
        when(es.submit(any(Callable.class))).thenReturn(mockFuture);
        when(mockFuture.get()).thenReturn(testAccount.getTransactionLogs().stream().map(TransactionLogDto::new).toList());
        
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
    public void 계좌에_500원을_입금한다() throws Exception {
        //given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return null; // 반환값이 필요 없으므로 null
        });

        //when
        es.submit(() -> {
            accountService.deposit(1L, 500L);
        });

        //then
        assertThat(testAccount.getBalance()).isEqualTo(1500L);
    }
    
    @Test
    public void 계좌에_5번_동시에_입금한다() throws Exception {
        //given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return null; // 반환값이 필요 없으므로 null
        });
        CountDownLatch latch = new CountDownLatch(5); // 입금 작업 개수만큼 카운트다운 설정
        
        //when
        for (int i = 0; i < 5; i++) {
            final long amount = 500L;

            es.submit(() -> {
                accountService.deposit(1L, amount);
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
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return null; // 반환값이 필요 없으므로 null
        });
        CountDownLatch latch = new CountDownLatch(5);

        // when
        for (int i = 0; i < 5; i++) {
            final long amount = 100L;
            es.submit(() -> {
                accountService.withdraw(1L, amount);
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
            return null; // 반환값이 필요 없으므로 null
        });

        // when
        // then
        assertThrows(RuntimeException.class, () -> accountService.withdraw(1L, 5000L));
    }
}