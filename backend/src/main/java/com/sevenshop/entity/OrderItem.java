package com.sevenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private String specs; // JSON 格式：{"甜度":"正常冰","加料":"珍珠"}
    private BigDecimal price; // 下单时的单价
    private Integer quantity;
    private LocalDateTime createdAt;
}
