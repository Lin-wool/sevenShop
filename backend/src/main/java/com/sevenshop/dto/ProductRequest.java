package com.sevenshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Long categoryId;
    private Integer status;
    private List<SpecItem> specs;

    @Data
    public static class SpecItem {
        private String specName;
        private List<String> specValues;
    }
}
