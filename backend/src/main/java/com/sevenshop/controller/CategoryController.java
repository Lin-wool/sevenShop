package com.sevenshop.controller;

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
    public ResponseEntity<?> createCategory(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            String name = (String) request.get("name");
            Integer sort = request.get("sort") != null ? (Integer) request.get("sort") : 0;
            Category category = categoryService.createCategory(name, sort);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            String name = (String) request.get("name");
            Integer sort = request.get("sort") != null ? (Integer) request.get("sort") : null;
            Category category = categoryService.updateCategory(id, name, sort);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, HttpServletRequest httpRequest) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        }
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
