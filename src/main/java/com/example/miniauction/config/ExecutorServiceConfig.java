package com.example.miniauction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorServiceConfig {
    @Bean
    public ExecutorService executorService() {
        int CORE_POOL_SIZE = 20;
        int MAX_POOL_SIZE = 50;
        int KEEP_ALIVE_TIME = 60;
        int QUEUE_CAPACITY = 1000;

        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    }
}
