package com.sevenshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_spec_template")
public class ProductSpecTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;        // 模板名称（奶茶、咖啡、甜品）
    private String specName;    // 规格名称（甜度、温度、加料）
    private String specValues;  // 规格值（逗号分隔）
    private Integer sort;       // 排序
}
