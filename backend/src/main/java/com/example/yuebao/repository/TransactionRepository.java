package com.example.yuebao.repository;

import com.example.yuebao.entity.Transaction;
import com.example.yuebao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}