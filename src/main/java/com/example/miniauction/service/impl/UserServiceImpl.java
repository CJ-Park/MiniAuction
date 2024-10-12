package com.example.miniauction.service.impl;

import com.example.miniauction.dto.user.UserCreateDto;
import com.example.miniauction.dto.user.UserLoginDto;
import com.example.miniauction.entity.Account;
import com.example.miniauction.entity.User;
import com.example.miniauction.repository.user.UserRepository;
import com.example.miniauction.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Long signUp(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        dto.encodingPassword();
        User user = User.createUser(dto);

        return userRepository.save(user).getId();
    }

    @Override
    public void signIn(UserLoginDto dto, HttpServletResponse response) {
        dto.encodingPassword();

        User user = userRepository.findByEmail(dto.getEmail());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        response.setHeader("AccessToken", String.valueOf(user.getId()));
    }

    @Override
    public Long getUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getAccount().getId();
    }

    @Override
    @Transactional
    public void connectAccount(Account saveAccount, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.connectAccount(saveAccount);
    }
}
