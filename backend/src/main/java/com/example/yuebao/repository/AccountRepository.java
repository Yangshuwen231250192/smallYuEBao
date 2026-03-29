package com.example.yuebao.repository;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByUserId(Long userId);
}