package com.sevenshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sevenshop.common.ApiResponse;
import com.sevenshop.common.BusinessException;
import com.sevenshop.dto.ProductRequest;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.ProductSpec;
import com.sevenshop.entity.Category;
import com.sevenshop.mapper.CategoryMapper;
import com.sevenshop.service.ProductService;
import com.sevenshop.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductRequest request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        Product product = productService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        Product product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        List<ProductSpec> specs = productService.getProductSpecs(id);
        List<Map<String, Object>> specList = new ArrayList<>();
        for (ProductSpec spec : specs) {
            Map<String, Object> specMap = new HashMap<>();
            specMap.put("id", spec.getId());
            specMap.put("specName", spec.getSpecName());
            specMap.put("specValues", productService.parseSpecValues(spec.getSpecValues()));
            specList.add(specMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", product.getId());
        result.put("name", product.getName());
        result.put("imageUrl", product.getImageUrl());
        result.put("price", product.getPrice());
        result.put("categoryId", product.getCategoryId());
        result.put("status", product.getStatus());
        result.put("createdAt", product.getCreatedAt());
        result.put("specs", specList);

        // 获取分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(product.getCategoryId());
            if (category != null) {
                result.put("categoryName", category.getName());
            }
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<Product> productPage = productService.getProductList(page, size, categoryId, keyword);

        // 批量查询分类信息，避免 N+1 查询
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!productPage.getRecords().isEmpty()) {
            List<Long> categoryIds = productPage.getRecords().stream()
                    .map(Product::getCategoryId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            if (!categoryIds.isEmpty()) {
                List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
                categoryNameMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("name", product.getName());
            productMap.put("imageUrl", product.getImageUrl());
            productMap.put("price", product.getPrice());
            productMap.put("categoryId", product.getCategoryId());
            productMap.put("status", product.getStatus());
            productMap.put("createdAt", product.getCreatedAt());

            // 使用批量查询的分类信息
            if (product.getCategoryId() != null) {
                productMap.put("categoryName", categoryNameMap.getOrDefault(product.getCategoryId(), ""));
            }

            records.add(productMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", productPage.getTotal());
        result.put("pages", productPage.getPages());
        result.put("current", productPage.getCurrent());

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
