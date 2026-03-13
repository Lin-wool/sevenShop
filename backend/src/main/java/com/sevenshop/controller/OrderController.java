package com.sevenshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sevenshop.dto.OrderRequest;
import com.sevenshop.entity.Order;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.User;
import com.sevenshop.mapper.ProductMapper;
import com.sevenshop.mapper.UserMapper;
import com.sevenshop.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Order order = orderService.createOrder(userId, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        try {
            Page<Order> orderPage = orderService.getUserOrders(userId, page, size, status);
            return ResponseEntity.ok(buildOrderListResponse(orderPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            Page<Order> orderPage = orderService.getAllOrders(page, size, status);
            return ResponseEntity.ok(buildOrderListResponse(orderPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "请先登录"));
        }
        String role = (String) httpRequest.getAttribute("role");

        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // 非管理员只能查看自己的订单
        if (!"ADMIN".equals(role) && !order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }

        return ResponseEntity.ok(buildOrderDetailResponse(order));
    }

    @PutMapping("/{id}/handle")
    public ResponseEntity<?> handleOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            orderService.handleOrder(id);
            return ResponseEntity.ok(Map.of("message", "处理成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Map<String, Object> buildOrderListResponse(Page<Order> orderPage) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            records.add(buildOrderDetailResponse(order));
        }
        return Map.of(
            "records", records,
            "total", orderPage.getTotal(),
            "pages", orderPage.getPages(),
            "current", orderPage.getCurrent()
        );
    }

    private Map<String, Object> buildOrderDetailResponse(Order order) {
        Product product = productMapper.selectById(order.getProductId());
        User user = userMapper.selectById(order.getUserId());

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("userId", order.getUserId());
        orderMap.put("productId", order.getProductId());
        orderMap.put("specs", orderService.parseSpecs(order.getSpecs()));
        orderMap.put("address", order.getAddress());
        orderMap.put("remark", order.getRemark());
        orderMap.put("status", order.getStatus());
        orderMap.put("price", order.getPrice());
        orderMap.put("quantity", order.getQuantity());
        orderMap.put("createdAt", order.getCreatedAt());
        orderMap.put("handledAt", order.getHandledAt());

        if (product != null) {
            orderMap.put("productName", product.getName());
            orderMap.put("productImage", product.getImageUrl());
        }

        if (user != null) {
            orderMap.put("userNickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
            orderMap.put("userEmail", user.getEmail());
        }

        return orderMap;
    }
}
