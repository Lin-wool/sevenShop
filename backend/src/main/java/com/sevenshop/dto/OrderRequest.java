package com.sevenshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderRequest {
    private Long productId;
    private Map<String, String> specs;
    private Long addressId;
    private String remark;
    private BigDecimal price;
    private Integer quantity;
}
