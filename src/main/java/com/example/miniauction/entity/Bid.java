package com.example.miniauction.entity;

import com.example.miniauction.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.miniauction.enums.BidStatus.OVERBID;

@Entity
@Table(name = "bids")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bidAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BidStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    public void updateStatus(BidStatus status) {
        this.status = status;
    }

    private Bid(Long bidAmount, BidStatus status, User user, Auction auction) {
        this.bidAmount = bidAmount;
        this.status = status;
        this.user = user;
        this.auction = auction;
    }

    public static Bid createBid(Long bidAmount, User user, Auction auction) {
        return new Bid(bidAmount, OVERBID, user, auction);
    }
}
