package com.example.miniauction.service.impl;

import com.example.miniauction.entity.Auction;
import com.example.miniauction.entity.Bid;
import com.example.miniauction.entity.User;
import com.example.miniauction.repository.auction.AuctionRepository;
import com.example.miniauction.repository.bid.BidRepository;
import com.example.miniauction.repository.user.UserRepository;
import com.example.miniauction.service.AccountService;
import com.example.miniauction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.miniauction.enums.BidStatus.*;
import static com.example.miniauction.enums.TransactionType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AccountService accountService;
    private final ExecutorService es;
    private final Lock lock = new ReentrantLock();

    @Override
    public void addBid(Long auctionId, Long bidAmount, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        es.submit(() -> {
            lock.lock();
            User previousBidder;
            Long previousBidAmount;

            try {
                Auction auction = auctionRepository.findById(auctionId)
                        .orElseThrow(() -> new RuntimeException("Auction not found"));

                previousBidder = auction.getHighestBidder();
                previousBidAmount = auction.getBidAmount();
                if (previousBidAmount >= bidAmount) {
                    throw new RuntimeException("Lower bid than the highest bid");
                }

                auction.updateBid(bidAmount);
                Bid bid = Bid.createBid(bidAmount, user, auction);
                bidRepository.save(bid);
            } finally {
                lock.unlock();
            }

            accountService.withdraw(user.getAccount().getId(), bidAmount, BIDDING);

            if (previousBidder != null) {
                es.submit(() -> {
                    try {
                        accountService.deposit(previousBidder.getAccount().getId(),
                                previousBidAmount, REFUND);

                        Auction auction = auctionRepository.findById(auctionId)
                                .orElseThrow(() -> new RuntimeException("Auction not found"));

                        Bid bidInfo = bidRepository.
                                findBidByAuctionAndUserAndStatus(auction, previousBidder, OVERBID);

                        if (bidInfo != null) {
                            bidInfo.updateStatus(FAILED);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected error", e);
                    }
                });
            }
        });
    }
}
