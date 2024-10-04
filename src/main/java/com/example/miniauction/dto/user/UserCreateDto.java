package com.example.miniauction.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
    private String email;
    private String password;
    private String nickname;
    private String phone;

    public void encodingPassword() {
        String originPass = this.password;
        // encoding..
        originPass += "a";
        this.password = originPass;
    }
}
