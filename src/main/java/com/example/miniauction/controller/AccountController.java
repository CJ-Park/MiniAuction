package com.example.miniauction.controller;

import com.example.miniauction.dto.account.AccountRequestDto;
import com.example.miniauction.dto.account.AccountDto;
import com.example.miniauction.service.AccountService;
import com.example.miniauction.util.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.miniauction.enums.TransactionType.DEPOSIT;
import static com.example.miniauction.enums.TransactionType.WITHDRAWAL;
import static com.example.miniauction.util.MyLogger.log;

/*
GET
계좌 조회

POST
계좌 입금/출금
*/
@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final TokenUtils tokenUtils;

    @GetMapping("")
    public AccountDto getAccount(HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        return accountService.getAccountBalance(userId);
    }

    @PostMapping("/deposit")
    public void deposit(@RequestBody AccountRequestDto dto, HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        accountService.deposit(dto, userId, DEPOSIT);
    }

    @PostMapping("/withdraw")
    public void withdraw(@RequestBody AccountRequestDto dto, HttpServletRequest request) {
        Long userId = tokenUtils.getUserId(request);
        accountService.withdraw(dto, userId, WITHDRAWAL);
    }
}
