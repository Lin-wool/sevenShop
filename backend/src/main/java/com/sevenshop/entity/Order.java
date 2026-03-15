package com.sevenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId; // 保留用于单商品下单兼容
    private String specs; // JSON 格式：{"甜度":"正常冰","加料":"珍珠"} // 保留用于单商品下单兼容
    private String address;
    private String remark;
    private Integer status; // 0: 待处理, 1: 已处理, -1: 已取消
    private BigDecimal price; // 保留用于单商品下单兼容（单价）
    private Integer quantity; // 保留用于单商品下单兼容
    private BigDecimal totalPrice; // 订单总金额
    private String cancelReason; // 取消原因
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private LocalDateTime canceledAt; // 取消时间

    // 订单项列表（非数据库字段）
    @TableField(exist = false)
    private List<OrderItem> items;
}
