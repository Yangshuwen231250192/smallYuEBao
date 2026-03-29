package com.example.yuebao.service;

import com.example.yuebao.entity.Account;
import com.example.yuebao.entity.User;
import com.example.yuebao.repository.AccountRepository;
import com.example.yuebao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String password, String phone, String email, String realName) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已注册");
        }
        
        // 检查邮箱是否已存在
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRealName(realName);
        
        User savedUser = userRepository.save(user);
        
        // 创建账户
        Account account = new Account();
        account.setUser(savedUser);
        accountRepository.save(account);
        
        return savedUser;
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 验证用户密码
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}