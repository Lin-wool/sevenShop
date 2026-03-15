package com.sevenshop.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchOrderRequest {
    private List<OrderItemRequest> items;
    private Long addressId;
    private String remark;
}
