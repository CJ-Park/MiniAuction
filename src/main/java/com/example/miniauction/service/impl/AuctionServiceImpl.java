package com.example.miniauction.service.impl;

import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.auction.AuctionDetailDto;
import com.example.miniauction.dto.auction.AuctionListDto;
import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.AuctionEndType;
import com.example.miniauction.repository.auction.AuctionRepository;
import com.example.miniauction.repository.user.UserRepository;
import com.example.miniauction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Override
    public List<AuctionListDto> getAuctionList() {
        List<Auction> auctions = auctionRepository.findAll();
        return auctions.stream().map(AuctionListDto::new).toList();
    }

    @Override
    public AuctionDetailDto getAuctionDetail(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        return new AuctionDetailDto(auction);
    }

    @Override
    @Transactional
    public void registerAuction(AuctionCreateDto auctionCreateDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime endDate = AuctionEndType.getByKey(auctionCreateDto.getEnd())
                .calculateEndDate(LocalDateTime.now());

        Auction auction = Auction.createAuction(auctionCreateDto, endDate, user);
        auctionRepository.save(auction);
    }

    // 경매 삭제는 입찰자가 없을 경우만 가능
    @Override
    @Transactional
    public void removeAuction(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (auction.getSeller() != user) {
            throw new RuntimeException("Only seller can delete auction");
        }

        if (auction.getBidAmount() != 0) {
            throw new RuntimeException("Already bidding, cannot delete auction");
        }

        auctionRepository.delete(auction);
    }
}
