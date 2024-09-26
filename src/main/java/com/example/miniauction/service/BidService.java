package com.example.miniauction.service;

public interface BidService {
    // 경매 물품 입찰
    boolean addBid(Long id, Long bidAmount);


}
