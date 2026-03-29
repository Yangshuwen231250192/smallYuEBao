package com.example.yuebao.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "alipay_balance", precision = 10, scale = 2)
    private BigDecimal alipayBalance = BigDecimal.valueOf(1000.00); // 默认1000元
    
    @Column(name = "yuebao_balance", precision = 10, scale = 2)
    private BigDecimal yuebaoBalance = BigDecimal.ZERO;
    
    @Column(name = "total_income", precision = 10, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;
    
    @Column(name = "last_interest_date")
    private LocalDateTime lastInterestDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastInterestDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}