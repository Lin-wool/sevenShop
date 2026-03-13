package com.sevenshop.controller;

import com.sevenshop.dto.LoginRequest;
import com.sevenshop.dto.RegisterRequest;
import com.sevenshop.entity.User;
import com.sevenshop.service.UserService;
import com.sevenshop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            User user = userService.getUserByUsername(request.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "role", user.getRole(),
                "email", user.getEmail() != null ? user.getEmail() : ""
            ));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.badRequest().body(Map.of("message", "token无效"));
            }
            String username = jwtUtil.getUsername(token);
            User user = userService.getUserByUsername(username);
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "role", user.getRole(),
                "email", user.getEmail() != null ? user.getEmail() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "获取用户信息失败"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.badRequest().body(Map.of("message", "token无效"));
            }
            Long userId = jwtUtil.getUserId(token);
            String nickname = request.get("nickname");
            String email = request.get("email");

            User user = userService.updateUserInfo(userId, nickname, email);
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "role", user.getRole(),
                "email", user.getEmail() != null ? user.getEmail() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.badRequest().body(Map.of("message", "token无效"));
            }
            Long userId = jwtUtil.getUserId(token);
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            userService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
