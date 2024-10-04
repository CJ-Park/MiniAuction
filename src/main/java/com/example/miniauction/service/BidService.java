package com.example.miniauction.service;

public interface BidService {
    // 경매 물품 입찰
    void addBid(Long auctionId, Long bidAmount, Long userId);

}
