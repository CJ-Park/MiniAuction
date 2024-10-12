package com.example.miniauction.controller;

import com.example.miniauction.dto.user.UserCreateDto;
import com.example.miniauction.dto.user.UserLoginDto;
import com.example.miniauction.service.AccountService;
import com.example.miniauction.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    private final AccountService accountService;

    @PostMapping("/join")
    public void signUp(@RequestBody UserCreateDto dto) {
        Long userId = userService.signUp(dto);
        accountService.generateAccount(userId);
    }

    @PostMapping("/login")
    public void login(@RequestBody UserLoginDto dto, HttpServletResponse response) {
        userService.signIn(dto, response);
    }
}
