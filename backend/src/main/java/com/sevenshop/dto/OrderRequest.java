package com.sevenshop.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
public class OrderRequest {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "规格不能为空")
    private Map<String, String> specs;

    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    private String remark;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    @Min(value = 99, message = "数量不能超过99")
    private Integer quantity;
}
