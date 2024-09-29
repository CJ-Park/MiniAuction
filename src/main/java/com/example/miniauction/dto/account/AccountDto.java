package com.example.miniauction.dto.account;

import com.example.miniauction.entity.Account;
import lombok.Getter;

@Getter
public class AccountDto {
    private final Long id;
    private final String accountNumber;
    private final Long balance;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
    }
}
