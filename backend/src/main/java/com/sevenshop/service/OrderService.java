package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sevenshop.dto.OrderRequest;
import com.sevenshop.entity.Address;
import com.sevenshop.entity.Order;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.User;
import com.sevenshop.mapper.AddressMapper;
import com.sevenshop.mapper.OrderMapper;
import com.sevenshop.mapper.ProductMapper;
import com.sevenshop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private MailService mailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Order createOrder(Long userId, OrderRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);

        // 计算总价：单价 * 数量
        java.math.BigDecimal unitPrice = request.getPrice() != null ? request.getPrice() : product.getPrice();
        order.setPrice(unitPrice.multiply(java.math.BigDecimal.valueOf(order.getQuantity())));

        try {
            order.setSpecs(objectMapper.writeValueAsString(request.getSpecs()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("规格序列化失败");
        }

        order.setAddress(address.getName() + " - " + address.getPhone() + " - " + address.getAddress());
        order.setRemark(request.getRemark());
        order.setStatus(0); // 待处理
        order.setCreatedAt(LocalDateTime.now());

        orderMapper.insert(order);

        // 发送邮件通知
        User user = userMapper.selectById(userId);
        try {
            mailService.sendNewOrderNotification(user, order, product);
        } catch (Exception e) {
            // 邮件发送失败不影响订单创建
            e.printStackTrace();
        }

        return order;
    }

    public Page<Order> getUserOrders(Long userId, Integer page, Integer size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        return orderMapper.selectPage(pageParam, wrapper);
    }

    public Page<Order> getAllOrders(Integer page, Integer size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        return orderMapper.selectPage(pageParam, wrapper);
    }

    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    public void handleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(1); // 已处理
        order.setHandledAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    public Map<String, String> parseSpecs(String specs) {
        try {
            return objectMapper.readValue(specs, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
