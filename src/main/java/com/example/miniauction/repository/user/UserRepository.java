package com.example.miniauction.repository.user;

import com.example.miniauction.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
