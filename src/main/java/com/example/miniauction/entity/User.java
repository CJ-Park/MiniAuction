package com.example.miniauction.entity;

import com.example.miniauction.dto.user.UserCreateDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phone;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    private List<Auction> auctions = new ArrayList<>();

    private User(String email, String password, String nickname, String phone) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
    }

    public static User createUser(UserCreateDto dto) {
        return new User(dto.getEmail(), dto.getPassword(), dto.getNickname(), dto.getPhone());
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }

    public List<Auction> getAuctions() {
        return Collections.unmodifiableList(auctions);
    }

    public void connectAccount(Account account) {
        this.account = account;
    }
}
