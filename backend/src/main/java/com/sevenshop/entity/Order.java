package com.sevenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private String specs; // JSON 格式：{"甜度":"正常冰","加料":"珍珠"}
    private String address;
    private String remark;
    private Integer status; // 0: 待处理, 1: 已处理
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
