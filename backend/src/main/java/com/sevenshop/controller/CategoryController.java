package com.sevenshop.controller;

import com.sevenshop.common.ApiResponse;
import com.sevenshop.common.BusinessException;
import com.sevenshop.entity.Category;
import com.sevenshop.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        String name = (String) request.get("name");
        Integer sort = request.get("sort") != null ? (Integer) request.get("sort") : 0;
        Category category = categoryService.createCategory(name, sort);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        String name = (String) request.get("name");
        Integer sort = request.get("sort") != null ? (Integer) request.get("sort") : null;
        Category category = categoryService.updateCategory(id, name, sort);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无权限");
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
