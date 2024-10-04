package com.example.miniauction.entity;

import com.example.miniauction.dto.auction.AuctionCreateDto;
import com.example.miniauction.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.example.miniauction.enums.AuctionStatus.PROGRESS;

@Entity
@Table(name = "auctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long startPrice;

    @Column(nullable = false)
    private Long bidAmount;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder; // 현재 최고 입찰자

    @Column(nullable = false)
    private AuctionStatus status;

    @Column(nullable = false)
    private LocalDateTime endDate;

    public void updateBid(Long bidAmount) {
        this.bidAmount = bidAmount;
    }

    private Auction(String title, String description, Long startPrice, LocalDateTime endDate,
                    User user) {
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.bidAmount = 0L;
        this.seller = user;
        this.status = PROGRESS;
        this.endDate = endDate;
    }

    public static Auction createAuction(AuctionCreateDto dto, LocalDateTime endDate, User user) {
        return new Auction(dto.getTitle(), dto.getDescription(), dto.getStartPrice(), endDate, user);
    }
}
