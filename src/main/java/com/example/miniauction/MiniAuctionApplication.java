package com.example.miniauction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
public class MiniAuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniAuctionApplication.class, args);
    }

}
