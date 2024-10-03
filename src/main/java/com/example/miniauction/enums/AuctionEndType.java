package com.example.miniauction.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuctionEndType {
    ONE("하루동안 경매 진행"),
    THREE("3일간 경매 진행"),
    FIVE("5일간 경매 진행");

    private final String description;
}
