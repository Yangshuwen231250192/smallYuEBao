package com.example.yuebao.controller;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.Transaction;
import com.example.yuebao.entity.User;
import com.example.yuebao.service.YuEBaoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余额宝控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class YuEBaoController {

    private final YuEBaoService yuEBaoService;

    /**
     * 获取当前用户账户信息
     */
    @GetMapping("/accounts")
    public ResponseEntity<?> getAccounts(Authentication authentication) {
        try {
            String username = authentication.getName();
            Account account = yuEBaoService.getUserAccount(username);
            
            AccountResponse response = new AccountResponse();
            response.setBalance(account.getAlipayBalance());
            response.setYuebao(account.getYuebaoBalance());
            response.setTotalIncome(account.getTotalIncome());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 转入操作
     */
    @PostMapping("/transfer/in")
    public ResponseEntity<?> transferIn(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            String username = authentication.getName();
            BigDecimal amount = new BigDecimal(body.get("amount"));
            
            Account account = yuEBaoService.transferIn(username, amount);
            
            AccountResponse response = new AccountResponse();
            response.setBalance(account.getAlipayBalance());
            response.setYuebao(account.getYuebaoBalance());
            response.setTotalIncome(account.getTotalIncome());
            response.setMessage("转入成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("操作失败"));
        }
    }

    /**
     * 转出操作
     */
    @PostMapping("/transfer/out")
    public ResponseEntity<?> transferOut(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            String username = authentication.getName();
            BigDecimal amount = new BigDecimal(body.get("amount"));
            
            Account account = yuEBaoService.transferOut(username, amount);
            
            AccountResponse response = new AccountResponse();
            response.setBalance(account.getAlipayBalance());
            response.setYuebao(account.getYuebaoBalance());
            response.setTotalIncome(account.getTotalIncome());
            response.setMessage("转出成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("操作失败"));
        }
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Transaction> transactions = yuEBaoService.getUserTransactions(username);
            
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 获取服务器IP地址（用于手机访问）
     */
    @GetMapping("/ip")
    public ResponseEntity<Map<String, String>> getServerIp() {
        try {
            String ip = getLocalIP();
            Map<String, String> response = new HashMap<>();
            response.put("ip", ip);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("ip", "localhost");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 获取本地 IP 地址（优先获取真实的物理网卡局域网 IP）
     */
    private String getLocalIP() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        
        System.out.println("=== 开始扫描网络接口 ===");
        
        // 收集所有非虚拟网卡的IP地址
        java.util.List<String> candidateIPs = new java.util.ArrayList<>();
        
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;
            
            String displayName = iface.getDisplayName().toLowerCase();
            String name = iface.getName().toLowerCase();
            
            // 跳过虚拟网卡（WSL、Hyper-V、VMware、VirtualBox等）
            if (displayName.contains("hyper-v") || displayName.contains("wsl") ||
                displayName.contains("vmware") || displayName.contains("virtualbox") ||
                displayName.contains("virtual") || name.contains("vbox") ||
                name.contains("vmnet") || name.contains("veth") ||
                name.contains("docker") || name.contains("br-")) {
                System.out.println("跳过虚拟网卡: " + displayName);
                continue;
            }
            
            // 优先处理无线网络
            boolean isWireless = displayName.contains("wireless") || displayName.contains("wlan") ||
                                displayName.contains("wi-fi") || displayName.contains("802.11");
            
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr.isLoopbackAddress() || addr.getHostAddress().contains(":")) continue;
                
                String ip = addr.getHostAddress();
                System.out.println("找到 IP: " + ip + " (接口: " + displayName + ")");
                
                // 优先返回无线网络IP
                if (isWireless) {
                    System.out.println("优先选择无线网络IP: " + ip);
                    return ip;
                }
                
                candidateIPs.add(ip);
            }
        }
        
        // 如果有候选IP，返回第一个
        if (!candidateIPs.isEmpty()) {
            String selectedIP = candidateIPs.get(0);
            System.out.println("从候选列表中选择IP: " + selectedIP);
            return selectedIP;
        }
        
        // 如果实在找不到，尝试获取主机IP
        try {
            String hostIP = InetAddress.getLocalHost().getHostAddress();
            if (!hostIP.equals("127.0.0.1") && !hostIP.equals("127.0.1.1")) {
                System.out.println("使用主机IP: " + hostIP);
                return hostIP;
            }
        } catch (Exception e) {
            System.out.println("获取主机IP失败: " + e.getMessage());
        }
        
        System.out.println("未找到合适的 IP，返回 localhost");
        return "localhost";
    }

    // DTO类
    @Data
    public static class AccountResponse {
        private BigDecimal balance;
        private BigDecimal yuebao;
        private BigDecimal totalIncome;
        private String message;
    }

    @Data
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}