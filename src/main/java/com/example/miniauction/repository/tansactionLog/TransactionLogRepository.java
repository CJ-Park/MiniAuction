package com.example.miniauction.repository.tansactionLog;

import com.example.miniauction.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
}
