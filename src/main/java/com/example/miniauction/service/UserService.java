package com.example.miniauction.service;

import com.example.miniauction.dto.user.UserCreateDto;
import com.example.miniauction.dto.user.UserLoginDto;
import com.example.miniauction.entity.Account;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    // 회원가입
    Long signUp(UserCreateDto dto);

    // 로그인
    void signIn(UserLoginDto dto, HttpServletResponse response);

    // 유저의 계좌 id 가져오기
    Long getUserAccount(Long userId);

    void connectAccount(Account saveAccount, Long userId);
}
