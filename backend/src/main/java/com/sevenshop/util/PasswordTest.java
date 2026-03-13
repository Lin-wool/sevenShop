package com.sevenshop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试数据库中的哈希值
        String dbHash = "$2a$10$EqKcp1WFKVQISheT3QxG/e6DXhK5FZ.8X.6L7mX5YzFXqLQzJxMGO";
        String plainPassword = "admin123";
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Database hash: " + dbHash);
        System.out.println("Matches: " + encoder.matches(plainPassword, dbHash));
        
        // 生成新的哈希值进行对比
        String newHash = encoder.encode(plainPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(plainPassword, newHash));
    }
}