package com.example.miniauction.repository.account;

import com.example.miniauction.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
