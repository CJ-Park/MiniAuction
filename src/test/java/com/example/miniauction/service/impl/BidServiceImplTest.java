package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.auction.AuctionBidDto;
import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.user.UserCreateDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.Bid;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.AuctionEndType;
import com.example.miniauction.enums.AuctionStatus;
import com.example.miniauction.enums.BidStatus;
import com.example.miniauction.enums.TransactionType;
import com.example.miniauction.repository.auction.AuctionRepository;
import com.example.miniauction.repository.bid.BidRepository;
import com.example.miniauction.repository.user.UserRepository;
import com.example.miniauction.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.miniauction.enums.TransactionType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {
    @Mock
    private BidRepository bidRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private BidServiceImpl bidService;

    private User user;

    private Auction auction;

    @BeforeEach
    void setUp() {
        user = createUser("mymail", "1234", "joe", "010-1234-1234");
        auction = createAuction("test1", "test1", 100L, 1);
        Account account = new Account(1L, "1234-5678", 1000L, new ArrayList<>());
        user.connectAccount(account);
    }

    private User createUser(String email, String password, String nickname, String phone) {
        UserCreateDto dto = new UserCreateDto(email, password, nickname, phone);
        dto.encodingPassword();
        return User.createUser(dto);
    }

    private Auction createAuction(String title, String description, Long startPrice, int endDay) {
        AuctionCreateDto dto = new AuctionCreateDto(title, description, startPrice, endDay);
        LocalDateTime endDate;
        LocalDateTime now = LocalDateTime.now();
        switch (AuctionEndType.getByKey(dto.getEnd())) {
            case ONE -> endDate = now.plusDays(1);
            case TWO -> endDate = now.plusDays(2);
            case THREE -> endDate = now.plusDays(3);
            case FOUR -> endDate = now.plusDays(4);
            case FIVE -> endDate = now.plusDays(5);
            default -> throw new IllegalStateException("Unexpected end type");
        }
        return new Auction(1L, dto.getTitle(), dto.getDescription(), dto.getStartPrice(),
                0L, user, null, AuctionStatus.PROGRESS, endDate);
    }

//    @Test
//    public void 경매_입찰_성공() throws Exception {
//        //given
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
//        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
//                .thenReturn(CompletableFuture.completedFuture(null));
//        AuctionBidDto dto = new AuctionBidDto(1L, 100L);
//
//        //when
//        bidService.addBid(dto, 1L);
//
//        //then
//        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
//        verify(bidRepository, times(1)).save(any());
//        verify(accountService, times(1))
//                .withdraw(captor.capture(), eq(1L), eq(BIDDING));
//        assertThat(captor.getValue().getAmount()).isEqualTo(dto.getBidAmount());
//    }

    @Test
    public void 입찰_갱신_시_이전_입찰자는_입찰금을_반환_받는다() throws Exception {
        //given
        User previousBidder = createUser("email2", "1212", "nick", "010-0011-0011");
        Long previousBidAmount = 500L;
        Account testAccount = new Account(2L, "1212-5678", 500L, new ArrayList<>());
        previousBidder.connectAccount(testAccount);
        Bid previousBid = Bid.createBid(previousBidAmount, previousBidder, auction);

        auction.updateBidInfo(previousBidder, previousBidAmount);

        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(bidRepository.findBidByAuctionAndUserAndStatus(any(Auction.class), any(User.class), any(BidStatus.class)))
                .thenReturn(previousBid);
        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        //when
        bidService.addBid(dto, 1L);

        //then
        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
        verify(accountService, times(1)).deposit(captor.capture(),
                eq(null), eq(REFUND));
        assertThat(captor.getValue().getAmount()).isEqualTo(previousBidAmount);
    }

    @Test
    public void 최고_입찰가보다_낮은_가격으로_입찰_시_예외가_발생한다() throws Exception {
        //given
        User previousBidder = createUser("email2", "1212", "nick", "010-0011-0011");
        Long previousBidAmount = 2500L;
        auction.updateBidInfo(previousBidder, previousBidAmount);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bidService.addBid(dto, 1L));
        assertThat(ex).hasMessage("Lower bid than the highest bid");
    }

    @Test
    public void 입찰_시_유저_정보가_없으면_예외가_발생한다() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bidService.addBid(dto, 1L));
        assertThat(ex).hasMessage("User not found");
    }

    @Test
    public void 최고가_입찰자가_재입찰_시_예외가_발생한다() throws Exception {
        //given
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);
        AuctionBidDto dto_2 = new AuctionBidDto(1L, 2000L);

        bidService.addBid(dto, 1L);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bidService.addBid(dto_2, 1L));
        assertThat(ex).hasMessage("Already the highest bidder");
    }

    @Test
    public void 입찰_시_경매_정보가_없으면_예외가_발생한다() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bidService.addBid(dto, 1L));
        assertThat(ex).hasMessage("Auction not found");
    }

    @Test
    public void 멀티스레드_경매_입찰_성공() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuctionBidDto dto = new AuctionBidDto(1L, 100L);

        //when
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> bidService.addBid(dto, 1L));
        future.join();

        //then
        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
        verify(bidRepository, times(1)).save(any());
        verify(accountService, times(1))
                .withdraw(captor.capture(), eq(1L), eq(BIDDING));
        assertThat(captor.getValue().getAmount()).isEqualTo(dto.getBidAmount());
    }
    
    @Test
    public void 멀티스레드_동시_입찰_시_하나만_성공() throws Exception {
        //given
        User user2 = createUser("ab", "ab", "ab", "ab");
        User user3 = createUser("cd", "cd", "cd", "cd");
        user2.connectAccount(new Account(2L, "1234-0000", 2000L, new ArrayList<>()));
        user3.connectAccount(new Account(3L, "1234-0100", 3000L, new ArrayList<>()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user3));
//        when(auctionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuctionBidDto dto = new AuctionBidDto(1L, 100L);

        //when
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> bidService.addBid(dto, 1L))
                .exceptionally(ex -> {
                    System.out.println(ex.getMessage());
                    return null;
                });
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> bidService.addBid(dto, 2L))
                .exceptionally(ex -> {
                    System.out.println(ex.getMessage());
                    return null;
                });
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> bidService.addBid(dto, 3L))
                .exceptionally(ex -> {
                    System.out.println(ex.getMessage());
                    return null;
                });

        CompletableFuture.allOf(future1, future2, future3).join();

        //then
        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
        verify(bidRepository, times(1)).save(any());
        verify(accountService, times(1))
                .withdraw(captor.capture(), anyLong(), eq(BIDDING));
        assertThat(captor.getValue().getAmount()).isEqualTo(dto.getBidAmount());
    } 

    //    @Test
//    public void 멀티스레드_입찰_갱신_시_이전_입찰자는_입찰금을_반환_받는다() throws Exception {
//        //given
//        User previousBidder = createUser("email2", "1212", "nick", "010-0011-0011");
//        Long previousBidAmount = 500L;
//        Account testAccount = new Account(2L, "1212-5678", 500L, new ArrayList<>());
//        previousBidder.connectAccount(testAccount);
//
//        auction.updateBidInfo(previousBidder, previousBidAmount);
//
//        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
//        when(accountService.withdraw(any(AccountRequestDto.class), anyLong(), any(TransactionType.class)))
//                .thenReturn(CompletableFuture.completedFuture(null));
//
//        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
//            Runnable task = invocation.getArgument(0);
//            task.run(); // 직접 실행
//            return CompletableFuture.completedFuture(null);
//        });
//
//        //when
//        bidService.addBid(dto, 1L);
//
//        //then
//        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
//        verify(accountService, times(1)).deposit(captor.capture(),
//                eq(null), eq(REFUND));
//        assertThat(captor.getValue().getAmount()).isEqualTo(previousBidAmount);
//    }

//    @Test
//    public void 멀티스레드_최고_입찰가보다_낮은_가격으로_입찰_시_예외가_발생한다() throws Exception {
//        //given
//        User previousBidder = createUser("email2", "1212", "nick", "010-0011-0011");
//        Long previousBidAmount = 2500L;
//        auction.updateBidInfo(previousBidder, previousBidAmount);
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
//
//        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
//            Runnable task = invocation.getArgument(0);
//            task.run();
//            return null;
//        });
//        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);
//
//        //when
//        //then
//        RuntimeException ex = assertThrows(RuntimeException.class,
//                () -> bidService.addBid(dto, 1L));
//        assertThat(ex).hasMessage("Lower bid than the highest bid");
//    }
}