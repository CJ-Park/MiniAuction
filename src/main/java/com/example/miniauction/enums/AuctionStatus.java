package com.example.miniauction.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuctionStatus {
    REJECTED("경매 유찰"),
    PROGRESS("경매 진행 중"),
    COMPLETE("경매 완료");

    private final String description;
}
