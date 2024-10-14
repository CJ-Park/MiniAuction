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
//@Transactional
/*
1. 트랜잭션 거는 경우 - 데이터베이스 레벨 락을 사용할 때
2. Lock 또는 synchronized 사용하는 경우에는 트랜잭션 사용 X => AOP 로 인해 락 적용 X
3. Tx 커밋은 무조건 Lock 해제 전에 진행 필요

결론
1. 트랜잭션과 영속성 컨텍스트의 이점을 모두 활용하며 데이터베이스 레벨의 락을 사용하기
2. 락을 걸어두고 트랜잭션이 걸려있는 메서드를 호출해서 트랜잭션 작업 성공적으로 마무리 후 락을 해제하기
ps. ConcurrentHashMap 을 통해 경매마다 동시성 제어 가능
 */
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AccountService accountService;
//    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<Long, Lock> auctionLocks = new ConcurrentHashMap<>();

    /*
    롤백되는 경우
    1. 이미 최고입찰자일 경우
    2. 잔고 부족할 경우 (출금 시 예외 받아야됨)
     */
    @Override
    public void addBid(AuctionBidDto dto, Long userId) {
        // 락을 걸고 Tx 걸린 메소드 호출해 동시성 제어
        Lock lock = auctionLocks.computeIfAbsent(dto.getAuctionId(), id -> new ReentrantLock());
        lock.lock();

        try {
            bidding(dto, userId);
        } finally {
            lock.unlock();
        }

        // 아래 경우는 데이터베이스 레벨의 비관적 락 사용해 동시성 제어 => 이때는 메소드 자체에 Tx 필요
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Auction auction = auctionRepository.findByIdWithLock(dto.getAuctionId())
//                .orElseThrow(() -> new RuntimeException("Auction not found"));
//
////        try {
//            User previousBidder = auction.getHighestBidder();
//            Long previousBidAmount = auction.getBidAmount();
//
//            if (previousBidder != null && previousBidder.equals(user)) {
//                log("이미 최고 입찰중임");
//                throw new RuntimeException("Already the highest bidder");
//            }
//
//            if (auction.getStartPrice() > dto.getBidAmount()) {
//                log("입찰시작가보다 낮은 입찰은 불가능");
//                throw new RuntimeException("Lower bid than the start price");
//            }
//
//            if (previousBidAmount >= dto.getBidAmount()) {
//                log("입찰금은 현재 최고 입찰가보다 높아야 됨");
//                throw new RuntimeException("Lower bid than the highest bid");
//            }
//
//            // 입찰 정보 갱신 및 Bid 저장
//            auction.updateBidInfo(user, dto.getBidAmount());
//            Bid bid = Bid.createBid(dto.getBidAmount(), user, auction);
//
//            // 출금 작업 수행
//            Future<?> future = accountService.withdraw(new AccountRequestDto(dto.getBidAmount()), userId, BIDDING);
//            try {
//                future.get();
//            } catch (Exception e) {
//                throw new RuntimeException("출금 실패 - " + e.getMessage());
//            }
//
//            if (previousBidder != null) {
//                log("이전 입찰자 id - " + previousBidder.getId());
//                log("이전 입찰금 - " + previousBidAmount);
//
//                // 환불 작업은 비동기적으로 처리
//                accountService.deposit(new AccountRequestDto(previousBidAmount), previousBidder.getId(), REFUND);
//
//                // 이전 입찰자 상태 업데이트
//                Bid bidInfo = bidRepository.findBidByAuctionAndUserAndStatus(auction, previousBidder, OVERBID);
//                bidInfo.updateStatus(FAILED);
//            }
////            em.flush();
//            bidRepository.save(bid);
    }

    // 원자적으로 처리할 작업 목록 (환불작업 제외)
    @Transactional
    public void bidding(AuctionBidDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Auction auction = auctionRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        User previousBidder = auction.getHighestBidder();
        Long previousBidAmount = auction.getBidAmount();

        if (previousBidder != null && previousBidder.equals(user)) {
            log("이미 최고 입찰중임");
            throw new RuntimeException("Already the highest bidder");
        }

        if (auction.getStartPrice() > dto.getBidAmount()) {
            log("입찰시작가보다 낮은 입찰은 불가능");
            throw new RuntimeException("Lower bid than the start price");
        }

        if (previousBidAmount >= dto.getBidAmount()) {
            log("입찰금은 현재 최고 입찰가보다 높아야 됨");
            throw new RuntimeException("Lower bid than the highest bid");
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
            log("이전 입찰자 id - " + previousBidder.getId());
            log("이전 입찰금 - " + previousBidAmount);

            // 환불 작업은 비동기적으로 처리
            accountService.deposit(new AccountRequestDto(previousBidAmount), previousBidder.getId(), REFUND);

            // 이전 입찰자 상태 업데이트
            Bid bidInfo = bidRepository.findBidByAuctionAndUserAndStatus(auction, previousBidder, OVERBID);
            bidInfo.updateStatus(FAILED);
        }

        bidRepository.save(bid);
    }
}
