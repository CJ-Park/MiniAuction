package com.example.miniauction.repository.bid;

import com.example.miniauction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
