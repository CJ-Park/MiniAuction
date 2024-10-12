package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.auction.AuctionBidDto;
import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.user.UserCreateDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.AuctionEndType;
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
import java.util.concurrent.ExecutorService;

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

    @Mock
    private ExecutorService es;

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
        return Auction.createAuction(dto, endDate, user);
    }

    @Test
    public void 경매_입찰_성공() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return null; // 반환값이 필요 없으므로 null
        });
        AuctionBidDto dto = new AuctionBidDto(1L, 100L);

        //when
        bidService.addBid(dto, 1L);

        //then
        ArgumentCaptor<AccountRequestDto> captor = ArgumentCaptor.forClass(AccountRequestDto.class);
        verify(bidRepository, times(1)).save(any());
        verify(accountService, times(1))
                .withdraw(captor.capture(), eq(1L), eq(BIDDING));
        assertThat(captor.getValue().getAmount()).isEqualTo(dto.getBidAmount());
    }

    @Test
    public void 입찰_갱신_시_이전_입찰자는_입찰금을_반환_받는다() throws Exception {
        //given
        User previousBidder = createUser("email2", "1212", "nick", "010-0011-0011");
        Long previousBidAmount = 500L;
        Account testAccount = new Account(2L, "1212-5678", 500L, new ArrayList<>());
        previousBidder.connectAccount(testAccount);

        auction.updateBidInfo(previousBidder, previousBidAmount);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run(); // 직접 실행
            return null; // 반환값이 필요 없으므로 null
        });
        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);

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
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        });
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
    public void 입찰_시_경매_정보가_없으면_예외가_발생한다() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        when(es.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        });
        AuctionBidDto dto = new AuctionBidDto(1L, 1000L);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bidService.addBid(dto, 1L));
        assertThat(ex).hasMessage("Auction not found");
    }
}