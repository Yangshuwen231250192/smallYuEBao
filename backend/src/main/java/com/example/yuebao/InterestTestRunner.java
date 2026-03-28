package com.example.yuebao;

import com.example.yuebao.model.AccountVO;
import com.example.yuebao.service.YuEBaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试组件 - 用于演示自动收益结算
 * 启动时会自动执行一次测试
 */
@Component
public class InterestTestRunner implements CommandLineRunner {

    @Autowired
    private YuEBaoService yuEBaoService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n");
        System.out.println("==========================================");
        System.out.println("   🧪 自动收益结算功能演示");
        System.out.println("==========================================");
        
        // 测试 1: 初始状态
        AccountVO accounts = yuEBaoService.getAccounts();
        System.out.println("📊 初始状态:");
        System.out.println("   余额账户：¥ " + accounts.getBalance());
        System.out.println("   余额宝：¥ " + accounts.getYuebao());
        
        // 测试 2: 转入操作
        System.out.println("\n💰 执行转入操作：¥500");
        yuEBaoService.transferIn(new BigDecimal("500"));
        accounts = yuEBaoService.getAccounts();
        System.out.println("   余额账户：¥ " + accounts.getBalance());
        System.out.println("   余额宝：¥ " + accounts.getYuebao());
        
        // 测试 3: 模拟一天后（通过反射修改 lastSettleTime）
        System.out.println("\n⏰ 模拟时间流逝到明天...");
        java.lang.reflect.Field field = YuEBaoService.class.getDeclaredField("lastSettleTime");
        field.setAccessible(true);
        field.set(yuEBaoService, LocalDateTime.now().minusDays(1));
        
        // 再次查询，触发自动结算
        accounts = yuEBaoService.getAccounts();
        System.out.println("✅ 自动结算完成！");
        System.out.println("   余额宝：¥ " + accounts.getYuebao());
        System.out.println("   收益：¥ " + accounts.getYuebao().subtract(new BigDecimal("500")));
        
        System.out.println("\n==========================================");
        System.out.println("💡 提示：刷新前端页面即可看到最新数据");
        System.out.println("==========================================\n");
    }
}