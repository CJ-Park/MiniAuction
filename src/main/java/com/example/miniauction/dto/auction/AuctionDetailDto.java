package com.example.miniauction.dto.auction;

import com.example.miniauction.entity.Auction;
import com.example.miniauction.enums.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AuctionDetailDto {
    private Long id;
    private String title;
    private String description;
    private Long startPrice;
    private Long bidAmount;
    private AuctionStatus status;
    private LocalDateTime endDate;

    public AuctionDetailDto(Auction auction) {
        this.id = auction.getId();
        this.title = auction.getTitle();
        this.description = auction.getDescription();
        this.startPrice = auction.getStartPrice();
        this.bidAmount = auction.getBidAmount();
        this.status = auction.getStatus();
        this.endDate = auction.getEndDate();
    }
}
