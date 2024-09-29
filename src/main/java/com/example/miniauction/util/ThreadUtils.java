package com.example.miniauction.util;

public abstract class ThreadUtils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("인터럽트 발생", e);
        }
    }
}
