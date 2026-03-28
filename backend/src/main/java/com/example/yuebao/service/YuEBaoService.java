package com.example.yuebao.service;

import com.example.yuebao.model.AccountVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 余额宝核心业务逻辑
 */
@Service
public class YuEBaoService {

    // 模拟数据库存储
    private BigDecimal balance = new BigDecimal("1000.00"); // 余额账户
    private BigDecimal yuebaoAmount = new BigDecimal("0.00"); // 余额宝资产
    private LocalDateTime lastSettleTime = LocalDateTime.now(); // 上次结算时间
    
    // 日利率：万分之五 (0.05%)
    private static final BigDecimal DAILY_RATE = new BigDecimal("0.0005"); 

    /**
     * 获取最新账户信息（触发自动收益结算）
     */
    public AccountVO getAccounts() {
        settleInterest(); // 每次查询前自动结算
        return new AccountVO(balance, yuebaoAmount);
    }

    /**
     * 核心逻辑：计算经过的天数并复利增加收益
     */
    private void settleInterest() {
        LocalDateTime now = LocalDateTime.now();
        // 计算相差天数（向下取整，不足一天不结算）
        long daysPassed = ChronoUnit.DAYS.between(lastSettleTime.toLocalDate(), now.toLocalDate());

        if (daysPassed > 0 && yuebaoAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 复利公式：本金 * (1 + 利率)^天数
            BigDecimal factor = BigDecimal.ONE.add(DAILY_RATE).pow((int) daysPassed);
            BigDecimal newAmount = yuebaoAmount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            
            // 更新金额和时间
            yuebaoAmount = newAmount;
            lastSettleTime = now;
            
            System.out.println("✅ 自动结算完成：经过 " + daysPassed + " 天，当前余额宝：" + yuebaoAmount);
        }
    }

    /**
     * 转入操作
     */
    public void transferIn(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转入金额必须为正数");
        }
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("转入金额不能超过余额账户的余额");
        }
        
        balance = balance.subtract(amount);
        yuebaoAmount = yuebaoAmount.add(amount);
        System.out.println("💰 转入成功：" + amount + "，余额：" + balance + "，余额宝：" + yuebaoAmount);
    }

    /**
     * 转出操作
     */
    public void transferOut(BigDecimal amount) {
        settleInterest(); // 转出前也要先结算
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转出金额必须为正数");
        }
        if (amount.compareTo(yuebaoAmount) > 0) {
            throw new IllegalArgumentException("转出金额不能超过余额宝账户的余额");
        }
        
        yuebaoAmount = yuebaoAmount.subtract(amount);
        balance = balance.add(amount);
        System.out.println("💸 转出成功：" + amount + "，余额：" + balance + "，余额宝：" + yuebaoAmount);
    }
}
