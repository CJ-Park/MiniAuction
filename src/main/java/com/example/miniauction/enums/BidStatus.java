package com.example.miniauction.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BidStatus {
    OVERBID("상회 입찰 중"),
    FAILED("입찰 실패"),
    SUCCESS("낙찰 완료");

    private final String description;
}
