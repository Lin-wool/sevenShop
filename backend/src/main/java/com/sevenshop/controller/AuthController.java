package com.sevenshop.controller;

import com.sevenshop.common.ApiResponse;
import com.sevenshop.common.BusinessException;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
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
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(400, "token无效");
        }
        String username = jwtUtil.getUsername(token);
        User user = userService.getUserByUsername(username);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        result.put("role", user.getRole());
        result.put("email", user.getEmail() != null ? user.getEmail() : "");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(400, "token无效");
        }
        Long userId = jwtUtil.getUserId(token);
        String nickname = request.get("nickname");
        String email = request.get("email");

        User user = userService.updateUserInfo(userId, nickname, email);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        result.put("role", user.getRole());
        result.put("email", user.getEmail() != null ? user.getEmail() : "");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isTokenValid(token)) {
            throw new BusinessException(400, "token无效");
        }
        Long userId = jwtUtil.getUserId(token);
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
