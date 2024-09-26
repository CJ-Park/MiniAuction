package com.example.miniauction.service.impl;

import com.example.miniauction.dto.auction.AuctionDetailDto;
import com.example.miniauction.dto.auction.AuctionListDto;
import com.example.miniauction.service.AuctionService;

import java.util.List;

public class AuctionServiceImpl implements AuctionService {
    @Override
    public List<AuctionListDto> getAuctionList() {
        return List.of();
    }

    @Override
    public AuctionDetailDto getAuctionDetail(Long id) {
        return null;
    }

    @Override
    public void registerAuction(String title, String description, Long startPrice) {

    }

    @Override
    public boolean removeAuction(Long id) {
        return false;
    }
}
