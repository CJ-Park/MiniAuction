package com.example.miniauction.dto.auction;

import com.example.miniauction.enums.AuctionEndType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionCreateDto {
    private String title;
    private String description;
    private Long startPrice;
    private int end;
}
