package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sevenshop.dto.LoginRequest;
import com.sevenshop.dto.RegisterRequest;
import com.sevenshop.entity.User;
import com.sevenshop.mapper.UserMapper;
import com.sevenshop.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    public User register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
    
    // 临时方法：生成BCrypt密码哈希（仅用于开发环境）
    public String generatePasswordHash(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    // 重置用户密码（仅用于开发环境）
    public void resetPassword(String username, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
        }
    }

    // 更新用户信息
    public User updateUserInfo(Long userId, String nickname, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (email != null) {
            user.setEmail(email);
        }
        userMapper.updateById(user);
        return user;
    }

    // 修改密码
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // 获取用户列表（分页）
    public IPage<User> getUserList(int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(pageParam, wrapper);
    }

    // 获取所有用户列表
    public java.util.List<User> getAllUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectList(wrapper);
    }

    // 更新用户状态
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
