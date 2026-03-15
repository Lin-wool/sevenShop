package com.sevenshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderItemRequest {
    private Long productId;
    private Map<String, String> specs;
    private BigDecimal price;
    private Integer quantity;
}
