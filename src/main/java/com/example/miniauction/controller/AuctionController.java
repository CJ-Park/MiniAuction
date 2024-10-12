package com.example.miniauction.controller;

import com.example.miniauction.dto.auction.AuctionBidDto;
import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.dto.auction.AuctionDetailDto;
import com.example.miniauction.dto.auction.AuctionListDto;
import com.example.miniauction.service.AuctionService;
import com.example.miniauction.service.BidService;
import com.example.miniauction.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
GET
경매 물품 리스트 조회
경매 물품 조회

POST
경매 물품 등록
경매 물품 입찰

DELETE
경매 물품 삭제
*/
@RequiredArgsConstructor
@RestController
@RequestMapping("/auction")
public class AuctionController {

    private final AuctionService auctionService;
    private final TokenUtils tokenUtils;
    private final BidService bidService;

    @GetMapping("")
    public List<AuctionListDto> getAuctionList() {
        return auctionService.getAuctionList();
    }

    @GetMapping("/{id}")
    public AuctionDetailDto getAuctionDetail(@PathVariable Long id) {
        return auctionService.getAuctionDetail(id);
    }

    @PostMapping("")
    public void addAuctionItem(@RequestBody AuctionCreateDto auctionCreateDto, HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        auctionService.registerAuction(auctionCreateDto, userId);
    }

    @PostMapping("/bid")
    public void addBid(@RequestBody AuctionBidDto bidDto, HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        bidService.addBid(bidDto, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteAuctionItem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        auctionService.removeAuction(id, userId);
    }
}
