package com.example.miniauction.repository.bid;

import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.Bid;
import com.example.miniauction.entity.User;
import com.example.miniauction.enums.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
    Bid findBidByAuctionAndUserAndStatus(Auction auction, User user, BidStatus status);
}
