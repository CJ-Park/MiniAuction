package com.example.miniauction.service;

import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.auction.AuctionDetailDto;
import com.example.miniauction.dto.auction.AuctionListDto;

import java.util.List;

public interface AuctionService {
    // 경매 물품 리스트 조회
    List<AuctionListDto> getAuctionList();

    // 경매 물품 조회
    AuctionDetailDto getAuctionDetail(Long id);

    // 경매 물품 등록
    void registerAuction(AuctionCreateDto auctionCreateDto);

    // 경매 물품 삭제
    void removeAuction(Long id);
}
