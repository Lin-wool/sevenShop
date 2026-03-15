package com.sevenshop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sevenshop.common.ApiResponse;
import com.sevenshop.entity.User;
import com.sevenshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public ApiResponse<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<User> result = userService.getUserList(page, size);
        return ApiResponse.success(result);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/all")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> result = userService.getAllUsers();
        return ApiResponse.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return ApiResponse.success(null);
    }
}
