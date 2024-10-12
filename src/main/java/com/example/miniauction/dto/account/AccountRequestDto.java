package com.example.miniauction.dto.account;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountRequestDto {
    private Long amount;

    public AccountRequestDto(Long amount) {
        this.amount = amount;
    }
}
