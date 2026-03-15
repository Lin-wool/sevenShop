package com.sevenshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sevenshop.common.ApiResponse;
import com.sevenshop.common.BusinessException;
import com.sevenshop.dto.BatchOrderRequest;
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
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody OrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Order order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Order>> createBatchOrder(@RequestBody BatchOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Order order = orderService.createBatchOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Page<Order> orderPage = orderService.getUserOrders(userId, page, size, status);
        return ResponseEntity.ok(ApiResponse.success(buildOrderListResponse(orderPage)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllOrders(
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        Page<Order> orderPage = orderService.getAllOrders(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(buildOrderListResponse(orderPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        String role = (String) httpRequest.getAttribute("role");

        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 非管理员只能查看自己的订单
        if (!"ADMIN".equals(role) && !order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限");
        }

        return ResponseEntity.ok(ApiResponse.success(buildOrderDetailResponse(order)));
    }

    @PutMapping("/{id}/handle")
    public ResponseEntity<ApiResponse<Void>> handleOrder(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        orderService.handleOrder(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Order order = orderService.getOrderById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 只能取消自己的订单
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限");
        }

        String cancelReason = request.get("cancelReason");
        orderService.cancelOrder(id, cancelReason);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private Map<String, Object> buildOrderListResponse(Page<Order> orderPage) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Order order : orderPage.getRecords()) {
            records.add(buildOrderDetailResponse(order));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", orderPage.getTotal());
        result.put("pages", orderPage.getPages());
        result.put("current", orderPage.getCurrent());
        return result;
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
        orderMap.put("totalPrice", order.getTotalPrice());
        orderMap.put("cancelReason", order.getCancelReason());
        orderMap.put("createdAt", order.getCreatedAt());
        orderMap.put("handledAt", order.getHandledAt());
        orderMap.put("canceledAt", order.getCanceledAt());

        if (product != null) {
            orderMap.put("productName", product.getName());
            orderMap.put("productImage", product.getImageUrl());
        }

        if (user != null) {
            orderMap.put("userNickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
            orderMap.put("userEmail", user.getEmail());
        }

        // 添加订单项信息
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (var item : order.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("productId", item.getProductId());
                itemMap.put("specs", orderService.parseSpecs(item.getSpecs()));
                itemMap.put("price", item.getPrice());
                itemMap.put("quantity", item.getQuantity());

                Product itemProduct = productMapper.selectById(item.getProductId());
                if (itemProduct != null) {
                    itemMap.put("productName", itemProduct.getName());
                    itemMap.put("productImage", itemProduct.getImageUrl());
                }
                itemsList.add(itemMap);
            }
            orderMap.put("items", itemsList);
        }

        return orderMap;
    }
}
