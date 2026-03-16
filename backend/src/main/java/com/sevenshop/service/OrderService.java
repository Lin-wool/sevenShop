package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sevenshop.dto.BatchOrderRequest;
import com.sevenshop.dto.OrderItemRequest;
import com.sevenshop.dto.OrderRequest;
import com.sevenshop.entity.Address;
import com.sevenshop.entity.Order;
import com.sevenshop.entity.OrderItem;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.User;
import com.sevenshop.mapper.AddressMapper;
import com.sevenshop.mapper.OrderItemMapper;
import com.sevenshop.mapper.OrderMapper;
import com.sevenshop.mapper.ProductMapper;
import com.sevenshop.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

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
        log.info("用户 {} 创建订单，商品ID: {}, 数量: {}", userId, request.getProductId(), request.getQuantity());

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

        // 计算总价：单价 * 数量（价格始终从商品表获取，防止客户端篡改）
        BigDecimal unitPrice = product.getPrice();
        order.setPrice(unitPrice.multiply(BigDecimal.valueOf(order.getQuantity())));
        order.setTotalPrice(order.getPrice()); // 兼容：单个商品时总价等于price

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

        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(request.getProductId());
        orderItem.setPrice(unitPrice);
        orderItem.setQuantity(order.getQuantity());
        try {
            orderItem.setSpecs(objectMapper.writeValueAsString(request.getSpecs()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("规格序列化失败");
        }
        orderItem.setCreatedAt(LocalDateTime.now());
        orderItemMapper.insert(orderItem);

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

    @Transactional
    public Order createBatchOrder(Long userId, BatchOrderRequest request) {
        log.info("用户 {} 创建批量订单，商品数量: {}", userId, request.getItems() != null ? request.getItems().size() : 0);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }

        // 计算订单总金额
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(address.getName() + " - " + address.getPhone() + " - " + address.getAddress());
        order.setRemark(request.getRemark());
        order.setStatus(0); // 待处理
        order.setCreatedAt(LocalDateTime.now());
        // 兼容旧字段，使用第一个商品的ID
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            order.setProductId(request.getItems().get(0).getProductId());
        }

        orderMapper.insert(order);

        // 批量查询商品信息，避免 N+1 查询
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 创建订单项
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemRequest.getProductId());
            }

            // 价格始终从商品表获取，防止客户端篡改
            BigDecimal itemPrice = product.getPrice();
            int quantity = itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1;
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
            totalPrice = totalPrice.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setPrice(itemPrice);
            orderItem.setQuantity(quantity);
            try {
                orderItem.setSpecs(objectMapper.writeValueAsString(itemRequest.getSpecs()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("规格序列化失败");
            }
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
        }

        // 更新订单总金额
        order.setTotalPrice(totalPrice);
        orderMapper.updateById(order);

        // 发送邮件通知（发送第一个商品的邮件）
        User user = userMapper.selectById(userId);
        Product firstProduct = productMapper.selectById(request.getItems().get(0).getProductId());
        try {
            mailService.sendNewOrderNotification(user, order, firstProduct);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return order;
    }

    @Transactional
    public void cancelOrder(Long orderId, String cancelReason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != 0) {
            if (order.getStatus() == 1) {
                throw new RuntimeException("该订单已处理，无法取消");
            } else if (order.getStatus() == -1) {
                throw new RuntimeException("该订单已取消");
            }
        }

        order.setStatus(-1); // 已取消
        order.setCancelReason(cancelReason);
        order.setCanceledAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    public Page<Order> getUserOrders(Long userId, Integer page, Integer size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        // 批量查询订单项，避免 N+1 查询
        if (!orderPage.getRecords().isEmpty()) {
            List<Long> orderIds = orderPage.getRecords().stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

            // 按订单ID分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            for (Order order : orderPage.getRecords()) {
                order.setItems(itemsMap.getOrDefault(order.getId(), new ArrayList<>()));
            }
        }

        return orderPage;
    }

    public Page<Order> getAllOrders(Integer page, Integer size, Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);

        // 批量查询订单项，避免 N+1 查询
        if (!orderPage.getRecords().isEmpty()) {
            List<Long> orderIds = orderPage.getRecords().stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());
            List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);

            // 按订单ID分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            for (Order order : orderPage.getRecords()) {
                order.setItems(itemsMap.getOrDefault(order.getId(), new ArrayList<>()));
            }
        }

        return orderPage;
    }

    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(id);
            order.setItems(items);
        }
        return order;
    }

    public void handleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == -1) {
            throw new RuntimeException("该订单已取消，无法处理");
        }
        order.setStatus(1); // 已处理
        order.setHandledAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    public Map<String, String> parseSpecs(String specs) {
        if (specs == null || specs.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(specs, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
