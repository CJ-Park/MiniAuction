package com.example.miniauction.enums;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
public enum AuctionEndType {
    ONE("하루동안 경매 진행", 1),
    TWO("2일간 경매 진행", 2),
    THREE("3일간 경매 진행", 3),
    FOUR("4일간 경매 진행", 4),
    FIVE("5일간 경매 진행", 5);

    private final String description;
    private final int days;

    public static AuctionEndType getByKey(int key) {
        return Arrays.stream(values()).filter(state -> state.days == key).findAny()
                .orElseThrow(() -> new RuntimeException("Illegal key: " + key));
    }

    public LocalDateTime calculateEndDate(LocalDateTime startDate) {
        return startDate.plusDays(this.days);
    }
}
