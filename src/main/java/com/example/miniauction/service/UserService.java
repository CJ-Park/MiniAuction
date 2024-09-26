package com.example.miniauction.service;

public interface UserService {
    // 회원가입
    boolean signUp(String email, String password, String nickname, String phone);

    // 로그인
    boolean signIn(String email, String password);
}
