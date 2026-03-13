package com.sevenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Long categoryId;
    private Integer status; // 0: 下架, 1: 上架
    private LocalDateTime createdAt;
}
