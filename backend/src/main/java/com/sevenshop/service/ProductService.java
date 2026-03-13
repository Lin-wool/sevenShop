package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sevenshop.dto.ProductRequest;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.ProductSpec;
import com.sevenshop.mapper.ProductMapper;
import com.sevenshop.mapper.ProductSpecMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSpecMapper productSpecMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setCreatedAt(LocalDateTime.now());

        productMapper.insert(product);

        // 保存规格
        if (request.getSpecs() != null) {
            for (ProductRequest.SpecItem spec : request.getSpecs()) {
                ProductSpec productSpec = new ProductSpec();
                productSpec.setProductId(product.getId());
                productSpec.setSpecName(spec.getSpecName());
                try {
                    productSpec.setSpecValues(objectMapper.writeValueAsString(spec.getSpecValues()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("规格序列化失败");
                }
                productSpecMapper.insert(productSpec);
            }
        }

        return product;
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        productMapper.updateById(product);

        // 删除旧规格，添加新规格
        LambdaQueryWrapper<ProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpec::getProductId, id);
        productSpecMapper.delete(wrapper);

        if (request.getSpecs() != null) {
            for (ProductRequest.SpecItem spec : request.getSpecs()) {
                ProductSpec productSpec = new ProductSpec();
                productSpec.setProductId(product.getId());
                productSpec.setSpecName(spec.getSpecName());
                try {
                    productSpec.setSpecValues(objectMapper.writeValueAsString(spec.getSpecValues()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("规格序列化失败");
                }
                productSpecMapper.insert(productSpec);
            }
        }

        return product;
    }

    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        LambdaQueryWrapper<ProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpec::getProductId, id);
        productSpecMapper.delete(wrapper);
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public Page<Product> getProductList(Integer page, Integer size, Long categoryId, String keyword) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Product::getName, keyword.trim());
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(pageParam, wrapper);
    }

    public List<ProductSpec> getProductSpecs(Long productId) {
        LambdaQueryWrapper<ProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpec::getProductId, productId);
        return productSpecMapper.selectList(wrapper);
    }

    public List<String> parseSpecValues(String specValues) {
        try {
            return objectMapper.readValue(specValues, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
