package com.example.miniauction.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {
    private String email;
    private String password;

    public void encodingPassword() {
        String originPass = this.password;
        // encoding..
        originPass += "a";
        this.password = originPass;
    }
}