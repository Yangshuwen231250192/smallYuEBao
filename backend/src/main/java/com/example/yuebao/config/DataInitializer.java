package com.example.yuebao.config;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.User;
import com.example.yuebao.repository.AccountRepository;
import com.example.yuebao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据初始化类 - 在应用启动时自动插入测试数据
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有测试用户
        if (!userRepository.findByUsername("demo").isPresent()) {
            initializeDemoData();
        }
    }
    
    private void initializeDemoData() {
        log.info("正在初始化演示数据...");
        
        // 创建演示用户
        User demoUser = new User();
        demoUser.setUsername("demo");
        demoUser.setPassword(passwordEncoder.encode("123456"));
        demoUser.setPhone("13800138000");
        demoUser.setEmail("demo@example.com");
        demoUser.setRealName("演示用户");
        demoUser.setCreatedAt(LocalDateTime.now());
        demoUser.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(demoUser);
        
        // 创建演示账户
        Account demoAccount = new Account();
        demoAccount.setUser(savedUser);
        demoAccount.setAlipayBalance(new BigDecimal("500.00"));
        demoAccount.setYuebaoBalance(new BigDecimal("500.25"));
        demoAccount.setTotalIncome(new BigDecimal("0.25"));
        demoAccount.setLastInterestDate(LocalDateTime.now());
        demoAccount.setCreatedAt(LocalDateTime.now());
        demoAccount.setUpdatedAt(LocalDateTime.now());
        
        accountRepository.save(demoAccount);
        
        log.info("演示数据初始化完成！");
        log.info("用户名: demo");
        log.info("密码: 123456");
        log.info("初始余额: 500.00");
        log.info("余额宝: 500.25 (包含0.25元收益)");
    }
}