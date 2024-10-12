package com.example.miniauction.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {
    public Long getUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new RuntimeException("Authorization header is null");
        }

        return Long.parseLong(authorization);
    }
}
