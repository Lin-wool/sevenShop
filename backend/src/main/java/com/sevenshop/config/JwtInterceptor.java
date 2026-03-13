package com.sevenshop.config;

import com.sevenshop.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenValid(token)) {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                // 设置到 request 属性中，供 controller 使用
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
            }
        }

        return true;
    }
}
