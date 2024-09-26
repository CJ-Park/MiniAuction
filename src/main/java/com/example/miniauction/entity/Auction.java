package com.example.miniauction.entity;

import com.example.miniauction.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Auction {
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
    @JoinColumn(name = "user_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private AuctionStatus status;

    @Column(nullable = false)
    private LocalDateTime endDate;
}
