package com.example.miniauction.service.impl;

import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.auction.AuctionBidDto;
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.miniauction.enums.BidStatus.*;
import static com.example.miniauction.enums.TransactionType.*;
import static com.example.miniauction.util.MyLogger.log;

@Service
@RequiredArgsConstructor
@Transactional
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AccountService accountService;
    private final ConcurrentHashMap<Long, Lock> auctionLocks = new ConcurrentHashMap<>();
//    private final ExecutorService es;
//    private final Lock lock = new ReentrantLock();

    /*
    롤백되는 경우
    1. 이미 최고입찰자일 경우
    2. 잔고 부족할 경우 (출금 시 예외 받아야됨)
     */
    @Override
    public void addBid(AuctionBidDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Auction auction = auctionRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        Lock lock = auctionLocks.computeIfAbsent(auction.getId(), id -> new ReentrantLock());

        lock.lock();
        try {
            User previousBidder = auction.getHighestBidder();
            Long previousBidAmount = auction.getBidAmount();

            if (previousBidAmount >= dto.getBidAmount()) {
                log("입찰금은 현재 최고 입찰가보다 높아야 됨");
                throw new RuntimeException("Lower bid than the highest bid");
            }

            if (previousBidder != null && previousBidder.equals(user)) {
                log("이미 최고 입찰중임");
                throw new RuntimeException("Already the highest bidder");
            }

            // 입찰 정보 갱신 및 Bid 저장
            auction.updateBidInfo(user, dto.getBidAmount());
            Bid bid = Bid.createBid(dto.getBidAmount(), user, auction);

            // 출금 작업 수행
            Future<?> future = accountService.withdraw(new AccountRequestDto(dto.getBidAmount()), userId, BIDDING);
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("출금 실패 - " + e.getMessage());
            }

            if (previousBidder != null) {
                // 환불 작업은 비동기적으로 처리
                accountService.deposit(new AccountRequestDto(previousBidAmount), previousBidder.getId(), REFUND);

                // 이전 입찰자 상태 업데이트
                Bid bidInfo = bidRepository.findBidByAuctionAndUserAndStatus(auction, previousBidder, OVERBID);
                bidInfo.updateStatus(FAILED);
            }
            bidRepository.save(bid);
        } finally {
            lock.unlock();
        }

        // 비동기 멀티스레드 처리 방식 -> 사용자가 예외를 받아보려면 이벤트 기반으로 사용자에게 푸쉬해줘야됨
//        es.submit(() -> {
//            lock.lock();
//            User previousBidder = auction.getHighestBidder();
//            Long previousBidAmount = auction.getBidAmount();
//
//            if (previousBidAmount >= dto.getBidAmount()) {
//                log("Lower bid than the highest bid");
//                throw new RuntimeException("Lower bid than the highest bid");
//            }
//
//            if (previousBidder != null && previousBidder.equals(user)) {
//                log("Already get the highest bid");
//                throw new RuntimeException("Already get the highest bid");
//            }
//
//            try {
//                auction.updateBidInfo(user, dto.getBidAmount());
//                Bid bid = Bid.createBid(dto.getBidAmount(), user, auction);
//                bidRepository.save(bid);
//
//                Future<?> future = accountService.withdraw(new AccountRequestDto(dto.getBidAmount()), userId, BIDDING);
//
//                try {
//                    future.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    log("출금 중 예외 발생 - " + e.getMessage());
//                    throw new RuntimeException(e);
//                }
//
//                auctionRepository.save(auction);
//
//                if (previousBidder != null) {
//                    es.submit(() -> {
//                        try {
//                            accountService.deposit(new AccountRequestDto(previousBidAmount),
//                                    previousBidder.getId(), REFUND);
//
//                            Bid bidInfo = bidRepository.
//                                    findBidByAuctionAndUserAndStatus(auction, previousBidder, OVERBID);
//
//                            if (bidInfo != null) {
//                                bidInfo.updateStatus(FAILED);
//                                bidRepository.save(bidInfo);
//                            }
//                        } catch (Exception e) {
//                            throw new RuntimeException("Unexpected error", e);
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            } finally {
//                lock.unlock();
//            }
//        });
    }
}
