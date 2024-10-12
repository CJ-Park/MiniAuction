package com.example.miniauction.service;

import com.example.miniauction.dto.auction.AuctionBidDto;

public interface BidService {
    // 경매 물품 입찰
    void addBid(AuctionBidDto dto, Long userId);

}
