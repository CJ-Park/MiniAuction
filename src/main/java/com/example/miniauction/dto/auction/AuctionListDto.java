package com.example.miniauction.dto.auction;

import com.example.miniauction.entity.Auction;
import com.example.miniauction.enums.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AuctionListDto {
    private Long id;
    private String title;
    private AuctionStatus status;
    private LocalDateTime endDate;

    public AuctionListDto(Auction auction) {
        this.id = auction.getId();
        this.title = auction.getTitle();
        this.status = auction.getStatus();
        this.endDate = auction.getEndDate();
    }
}
