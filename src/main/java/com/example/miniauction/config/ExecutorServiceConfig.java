package com.example.miniauction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class ExecutorServiceConfig implements AsyncConfigurer {
    final int CORE_POOL_SIZE = 20;
    final int MAX_POOL_SIZE = 50;
    final int KEEP_ALIVE_TIME = 60;
    final int QUEUE_CAPACITY = 1000;

    @Bean
    public ExecutorService executorService() {

        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY));
    }

    // @Async 로 동작하는 입출금 로그 작성만을 위한 별도 스레드 풀 생성
    @Override
    public Executor getAsyncExecutor() {
        return new ThreadPoolTaskExecutor() {{
            setCorePoolSize(CORE_POOL_SIZE);
            setMaxPoolSize(MAX_POOL_SIZE);
            setQueueCapacity(QUEUE_CAPACITY);
            setThreadNamePrefix("LogAsync-");
            initialize();
        }};
    }
}
