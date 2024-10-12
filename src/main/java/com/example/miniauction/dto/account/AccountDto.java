package com.example.miniauction.dto.account;

import com.example.miniauction.entity.Account;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Long balance;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
    }
}
