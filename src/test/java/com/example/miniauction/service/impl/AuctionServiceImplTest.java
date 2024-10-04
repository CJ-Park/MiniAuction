package com.example.miniauction.service.impl;

import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.auction.AuctionDetailDto;
import com.example.miniauction.dto.auction.AuctionListDto;
import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.AuctionEndType;
import com.example.miniauction.repository.auction.AuctionRepository;
import com.example.miniauction.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionServiceImplTest {
    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User mockUser;

    @Mock
    private User mockUser_2;

    @InjectMocks
    private AuctionServiceImpl auctionService;

    private List<Auction> auctions = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        Auction auction_1 = createAuction("test1", "test1", 100L, 1);
        Auction auction_2 = createAuction("test2", "test2", 200L, 3);
        Auction auction_3 = createAuction("test3", "test3", 300L, 5);

        auctions.addAll(List.of(auction_1, auction_2, auction_3));
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
        return Auction.createAuction(dto, endDate, mockUser);
    }

    @Test
    public void 경매_리스트_조회() throws Exception {
        //given
        when(auctionRepository.findAll()).thenReturn(auctions);

        //when
        List<AuctionListDto> listDto = auctionService.getAuctionList();

        //then
        assertThat(listDto).hasSize(3);
    }

    @Test
    public void 경매_조회() throws Exception {
        //given
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auctions.get(0)));

        //when
        AuctionDetailDto auctionDetail = auctionService.getAuctionDetail(1L);

        //then
        assertThat(auctionDetail).isNotNull();
        assertThat(auctionDetail.getTitle()).isEqualTo("test1");
        assertThat(auctionDetail.getDescription()).isEqualTo("test1");
        assertThat(auctionDetail.getStartPrice()).isEqualTo(100L);
        assertThat(auctionDetail.getEndDate()).isAfter(LocalDateTime.now());
    }

    @Test
    public void 존재하지_않는_경매_조회() throws Exception {
        //given
        when(auctionRepository.findById(4L)).thenReturn(Optional.empty());

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> auctionService.getAuctionDetail(4L));
        assertThat(ex.getMessage()).isEqualTo("Auction not found");
    }

    @Test
    public void 유저는_경매를_등록할_수_있음() throws Exception {
        //given
        AuctionCreateDto dto = new AuctionCreateDto("test5", "description", 500L, 5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        //when
        auctionService.registerAuction(dto);

        //then
        verify(auctionRepository).save(any(Auction.class));
    }

    @Test
    public void 경매_종료_타입을_잘못_등록하면_예외가_발생() throws Exception {
        //given
        AuctionCreateDto dto = new AuctionCreateDto("test5", "description", 500L,
                10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> auctionService.registerAuction(dto));
        assertThat(ex).hasMessage("Illegal key: " + 10);
    }

    @Test
    public void 유저_정보가_없으면_경매_등록에_실패함() throws Exception {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        AuctionCreateDto dto = new AuctionCreateDto("test5", "description", 500L,
                10);

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> auctionService.registerAuction(dto));
        assertThat(ex).hasMessage("User not found");
    }

    @Test
    public void 판매자가_아니면_경매를_삭제_시_예외가_발생() throws Exception {
        //given
        Auction auction = createAuction("delete", "description", 100L, 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser_2));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> auctionService.removeAuction(1L));
        assertThat(ex).hasMessage("Only seller can delete auction");
    }

    @Test
    public void 이미_입찰된_경매면_삭제_시_예외가_발생() throws Exception {
        //given
        Auction auction = createAuction("delete", "description", 100L, 1);
        auction.updateBid(100L);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        //when
        //then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> auctionService.removeAuction(1L));
        assertThat(ex).hasMessage("Already bidding, cannot delete auction");
    }
}