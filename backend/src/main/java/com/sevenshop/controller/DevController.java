package com.sevenshop.controller;

import com.sevenshop.service.UserService;
import com.sevenshop.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/generate-password")
    public Map<String, String> generatePassword(@RequestParam String password) {
        String hash = userService.generatePasswordHash(password);
        return Map.of(
            "plainPassword", password,
            "hashedPassword", hash
        );
    }

    @GetMapping("/reset-password")
    public Map<String, String> resetPassword(
            @RequestParam String username,
            @RequestParam String newPassword) {
        userService.resetPassword(username, newPassword);
        return Map.of(
            "message", "密码重置成功",
            "username", username,
            "newPasswordHash", userService.generatePasswordHash(newPassword)
        );
    }

    @GetMapping("/debug-token")
    public Map<String, Object> debugToken(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        result.put("authHeader", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            result.put("token", token);

            try {
                boolean isValid = jwtUtil.isTokenValid(token);
                result.put("isValid", isValid);

                if (isValid) {
                    Long userId = jwtUtil.getUserId(token);
                    String username = jwtUtil.getUsername(token);
                    String role = jwtUtil.getRole(token);

                    result.put("userId", userId);
                    result.put("username", username);
                    result.put("role", role);
                }
            } catch (Exception e) {
                result.put("error", e.getMessage());
            }
        }

        // 检查 request 属性
        result.put("request_userId", request.getAttribute("userId"));
        result.put("request_username", request.getAttribute("username"));
        result.put("request_role", request.getAttribute("role"));

        return result;
    }
}