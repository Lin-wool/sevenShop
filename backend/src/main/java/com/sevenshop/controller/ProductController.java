package com.sevenshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sevenshop.dto.ProductRequest;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.ProductSpec;
import com.sevenshop.entity.Category;
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

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            Product product = productService.createProduct(request);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            Product product = productService.updateProduct(id, request);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
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

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Product> productPage = productService.getProductList(page, size, categoryId, keyword);

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

                // 获取分类名称
                if (product.getCategoryId() != null) {
                    Category category = categoryService.getCategoryById(product.getCategoryId());
                    if (category != null) {
                        productMap.put("categoryName", category.getName());
                    }
                }

                records.add(productMap);
            }

            return ResponseEntity.ok(Map.of(
                "records", records,
                "total", productPage.getTotal(),
                "pages", productPage.getPages(),
                "current", productPage.getCurrent()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
