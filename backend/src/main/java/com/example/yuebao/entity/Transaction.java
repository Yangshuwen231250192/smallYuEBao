package com.example.yuebao.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "alipay_balance_after", precision = 10, scale = 2)
    private BigDecimal alipayBalanceAfter;
    
    @Column(name = "yuebao_balance_after", precision = 10, scale = 2)
    private BigDecimal yuebaoBalanceAfter;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum TransactionType {
        TRANSFER_IN,    // 转入余额宝
        TRANSFER_OUT,   // 转出余额宝
        INTEREST        // 利息收益
    }
}