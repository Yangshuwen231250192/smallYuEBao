package com.example.yuebao.controller;

import com.example.yuebao.entity.User;
import com.example.yuebao.service.UserService;
import com.example.yuebao.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("=== 登录请求 ===");
            System.out.println("用户名：" + request.getUsername());
            
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);
            
            System.out.println("登录成功，生成 JWT token");
            return ResponseEntity.ok(new LoginResponse(jwt, "登录成功"));
        } catch (Exception e) {
            System.err.println("登录失败：" + e.getMessage());
            e.printStackTrace();
            
            // 区分不同的错误情况
            if (e instanceof org.springframework.security.authentication.BadCredentialsException) {
                return ResponseEntity.badRequest().body(new ErrorResponse("用户名或密码错误"));
            } else if (e instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
                return ResponseEntity.badRequest().body(new ErrorResponse("用户不存在"));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("登录失败：" + e.getMessage()));
            }
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getPhone(),
                request.getEmail(),
                request.getRealName()
            );
            
            return ResponseEntity.ok(new MessageResponse("注册成功，请登录"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userService.findByUsername(username).isPresent();
        return ResponseEntity.ok(new CheckResponse(!exists));
    }
    
    /**
     * 测试端点：检查演示用户数据
     */
    @GetMapping("/test-demo-user")
    public ResponseEntity<?> testDemoUser() {
        try {
            java.util.Optional<User> userOpt = userService.findByUsername("demo");
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return ResponseEntity.ok(new DemoUserResponse(
                    user.getUsername(),
                    user.getPassword(),
                    "用户存在"
                ));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("演示用户不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("检查失败: " + e.getMessage()));
        }
    }
    
    // DTO类
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
        
        private String phone;
        private String email;
        private String realName;
    }
    
    @Data
    public static class LoginResponse {
        private String token;
        private String message;
        
        public LoginResponse(String token, String message) {
            this.token = token;
            this.message = message;
        }
    }
    
    @Data
    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
    }
    
    @Data
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
    
    @Data
    public static class CheckResponse {
        private boolean available;
        
        public CheckResponse(boolean available) {
            this.available = available;
        }
    }
    
    @Data
    public static class DemoUserResponse {
        private String username;
        private String password;
        private String message;
        
        public DemoUserResponse(String username, String password, String message) {
            this.username = username;
            this.password = password;
            this.message = message;
        }
    }
}