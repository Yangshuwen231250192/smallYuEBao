package com.example.yuebao.controller;

import com.example.yuebao.model.AccountVO;
import com.example.yuebao.service.YuEBaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 余额宝控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许 React 前端跨域访问
public class YuEBaoController {

    @Autowired
    private YuEBaoService yuEBaoService;

    /**
     * 获取账户信息
     */
    @GetMapping("/accounts")
    public ResponseEntity<AccountVO> getAccounts() {
        AccountVO accounts = yuEBaoService.getAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * 转入操作
     */
    @PostMapping("/transferIn")
    public ResponseEntity<?> transferIn(@RequestBody Map<String, String> body) {
        try {
            BigDecimal amount = new BigDecimal(body.get("amount"));
            yuEBaoService.transferIn(amount);
            return ResponseEntity.ok(yuEBaoService.getAccounts());
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "操作失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 转出操作
     */
    @PostMapping("/transferOut")
    public ResponseEntity<?> transferOut(@RequestBody Map<String, String> body) {
        try {
            BigDecimal amount = new BigDecimal(body.get("amount"));
            yuEBaoService.transferOut(amount);
            return ResponseEntity.ok(yuEBaoService.getAccounts());
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "操作失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取本地IP地址
     */
    @GetMapping("/ip")
    public ResponseEntity<Map<String, String>> getLocalIP() {
        try {
            String ip = getLocalHostLANAddress().getHostAddress();
            
            Map<String, String> response = new HashMap<>();
            response.put("ip", ip);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("ip", "localhost");
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 获取正确的本地局域网IP地址（避免获取WSL虚拟网络地址）
     */
    private InetAddress getLocalHostLANAddress() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            
            // 跳过回环接口和未启用的接口
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            
            // 跳过虚拟网络接口（如WSL、Docker等）
            String displayName = networkInterface.getDisplayName().toLowerCase();
            if (displayName.contains("wsl") || displayName.contains("docker") || 
                displayName.contains("hyper-v") || displayName.contains("virtual") ||
                displayName.contains("vmware") || displayName.contains("vbox")) {
                continue;
            }
            
            // 检查无线网络接口
            if (displayName.contains("wireless") || displayName.contains("wlan") || 
                displayName.contains("wi-fi") || displayName.contains("无线")) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    // 返回IPv4地址
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        return address;
                    }
                }
            }
            
            // 检查有线网络接口
            if (displayName.contains("ethernet") || displayName.contains("本地连接") || 
                displayName.contains("以太网") || displayName.contains("lan")) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    // 返回IPv4地址
                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        return address;
                    }
                }
            }
        }
        
        // 如果没有找到合适的接口，返回默认的本地地址
        return InetAddress.getLocalHost();
    }
}