package com.example.yuebao;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.User;
import com.example.yuebao.repository.AccountRepository;
import com.example.yuebao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试组件 - 用于演示自动收益结算
 * 启动时会自动执行一次测试
 */
@Component
@RequiredArgsConstructor
public class InterestTestRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n");
        System.out.println("==========================================");
        System.out.println("   🧪 自动收益结算功能演示");
        System.out.println("==========================================");
        
        // 确保演示用户存在
        ensureDemoUser();
        
        // 获取演示账户
        User demoUser = userRepository.findByUsername("demo")
                .orElseThrow(() -> new RuntimeException("演示用户不存在"));
        Account account = accountRepository.findByUser(demoUser)
                .orElseThrow(() -> new RuntimeException("演示账户不存在"));
        
        // 测试 1: 初始状态
        System.out.println("📊 初始状态:");
        System.out.println("   余额账户：¥ " + account.getAlipayBalance());
        System.out.println("   余额宝：¥ " + account.getYuebaoBalance());
        
        // 测试 2: 转入操作
        System.out.println("\n💰 执行转入操作：¥500");
        account.setAlipayBalance(account.getAlipayBalance().subtract(new BigDecimal("500")));
        account.setYuebaoBalance(account.getYuebaoBalance().add(new BigDecimal("500")));
        accountRepository.save(account);
        
        System.out.println("   余额账户：¥ " + account.getAlipayBalance());
        System.out.println("   余额宝：¥ " + account.getYuebaoBalance());
        
        // 测试 3: 模拟一天后（通过修改 lastInterestDate）
        System.out.println("\n⏰ 模拟时间流逝到明天...");
        account.setLastInterestDate(LocalDateTime.now().minusDays(1));
        accountRepository.save(account);
        
        // 再次查询，触发自动结算
        Account updatedAccount = accountRepository.findByUser(demoUser)
                .orElseThrow(() -> new RuntimeException("演示账户不存在"));
        
        System.out.println("✅ 自动结算完成！");
        System.out.println("   余额宝：¥ " + updatedAccount.getYuebaoBalance());
        System.out.println("   收益：¥ " + updatedAccount.getTotalIncome());
        System.out.println("==========================================");
        System.out.println("💡 提示：使用演示账户登录即可体验完整功能");
        System.out.println("==========================================\n");
    }
    
    private void ensureDemoUser() {
        if (userRepository.findByUsername("demo").isPresent()) {
            return;
        }
        
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
        demoAccount.setAlipayBalance(new BigDecimal("1000.00"));
        demoAccount.setYuebaoBalance(new BigDecimal("0.00"));
        demoAccount.setTotalIncome(new BigDecimal("0.00"));
        demoAccount.setLastInterestDate(LocalDateTime.now());
        demoAccount.setCreatedAt(LocalDateTime.now());
        demoAccount.setUpdatedAt(LocalDateTime.now());
        
        accountRepository.save(demoAccount);
    }
}