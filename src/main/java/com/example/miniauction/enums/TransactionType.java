package com.example.miniauction.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    DEPOSIT("입금"), WITHDRAWAL("출금"),
    BIDDING("입찰"), BID_RETURN("입찰금 반환");

    private final String description;
}
