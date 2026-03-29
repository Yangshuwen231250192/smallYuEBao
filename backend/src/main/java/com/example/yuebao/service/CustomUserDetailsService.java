package com.example.yuebao.service;

import com.example.yuebao.entity.User;
import com.example.yuebao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== 加载用户详情 ===");
        System.out.println("用户名：" + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("用户不存在：" + username);
                    return new UsernameNotFoundException("用户不存在：" + username);
                });
        
        System.out.println("用户加载成功：" + user.getUsername());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}