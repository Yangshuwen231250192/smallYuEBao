package com.example.yuebao.service;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.Transaction;
import com.example.yuebao.entity.User;
import com.example.yuebao.repository.AccountRepository;
import com.example.yuebao.repository.TransactionRepository;
import com.example.yuebao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 余额宝核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class YuEBaoService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    
    // 日利率：万分之五 (0.05%)
    private static final BigDecimal DAILY_RATE = new BigDecimal("0.0005"); 

    /**
     * 获取用户账户信息（触发自动收益结算）
     */
    @Transactional
    public Account getUserAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        settleInterest(account); // 每次查询前自动结算
        
        return accountRepository.save(account); // 保存更新后的账户信息
    }

    /**
     * 核心逻辑：计算经过的天数并复利增加收益
     */
    private void settleInterest(Account account) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSettleTime = account.getLastInterestDate();
        
        // 计算相差天数（向下取整，不足一天不结算）
        long daysPassed = ChronoUnit.DAYS.between(lastSettleTime.toLocalDate(), now.toLocalDate());

        if (daysPassed > 0 && account.getYuebaoBalance().compareTo(BigDecimal.ZERO) > 0) {
            // 复利公式：本金 * (1 + 利率)^天数
            BigDecimal factor = BigDecimal.ONE.add(DAILY_RATE).pow((int) daysPassed);
            BigDecimal newAmount = account.getYuebaoBalance().multiply(factor)
                    .setScale(2, RoundingMode.HALF_UP);
            
            // 计算收益
            BigDecimal interest = newAmount.subtract(account.getYuebaoBalance());
            
            // 更新账户信息
            account.setYuebaoBalance(newAmount);
            account.setTotalIncome(account.getTotalIncome().add(interest));
            account.setLastInterestDate(now);
            
            // 记录利息交易
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                Transaction transaction = new Transaction();
                transaction.setUser(account.getUser());
                transaction.setType(Transaction.TransactionType.INTEREST);
                transaction.setAmount(interest);
                transaction.setAlipayBalanceAfter(account.getAlipayBalance());
                transaction.setYuebaoBalanceAfter(account.getYuebaoBalance());
                transaction.setDescription(String.format("余额宝利息收益 %.2f元", interest));
                transactionRepository.save(transaction);
            }
            
            System.out.println("✅ 自动结算完成：用户 " + account.getUser().getUsername() + 
                    "，经过 " + daysPassed + " 天，利息：" + interest + "元");
        }
    }

    /**
     * 转入余额宝
     */
    @Transactional
    public Account transferIn(String username, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转入金额必须大于0");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        if (account.getAlipayBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        
        // 更新余额
        account.setAlipayBalance(account.getAlipayBalance().subtract(amount));
        account.setYuebaoBalance(account.getYuebaoBalance().add(amount));
        
        Account savedAccount = accountRepository.save(account);
        
        // 记录交易
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.TRANSFER_IN);
        transaction.setAmount(amount);
        transaction.setAlipayBalanceAfter(savedAccount.getAlipayBalance());
        transaction.setYuebaoBalanceAfter(savedAccount.getYuebaoBalance());
        transaction.setDescription(String.format("转入余额宝 %.2f元", amount));
        transactionRepository.save(transaction);
        
        System.out.println("💰 转入成功：用户 " + username + 
                "，金额：" + amount + 
                "，余额：" + savedAccount.getAlipayBalance() + 
                "，余额宝：" + savedAccount.getYuebaoBalance());
        
        return savedAccount;
    }

    /**
     * 转出余额宝
     */
    @Transactional
    public Account transferOut(String username, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("转出金额必须大于0");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        
        if (account.getYuebaoBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额宝余额不足");
        }
        
        // 更新余额
        account.setAlipayBalance(account.getAlipayBalance().add(amount));
        account.setYuebaoBalance(account.getYuebaoBalance().subtract(amount));
        
        Account savedAccount = accountRepository.save(account);
        
        // 记录交易
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType(Transaction.TransactionType.TRANSFER_OUT);
        transaction.setAmount(amount);
        transaction.setAlipayBalanceAfter(savedAccount.getAlipayBalance());
        transaction.setYuebaoBalanceAfter(savedAccount.getYuebaoBalance());
        transaction.setDescription(String.format("转出余额宝 %.2f元", amount));
        transactionRepository.save(transaction);
        
        System.out.println("💰 转出成功：用户 " + username + 
                "，金额：" + amount + 
                "，余额：" + savedAccount.getAlipayBalance() + 
                "，余额宝：" + savedAccount.getYuebaoBalance());
        
        return savedAccount;
    }

    /**
     * 获取用户交易记录
     */
    public List<Transaction> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}