package com.sevenshop.controller;

import com.sevenshop.common.ApiResponse;
import com.sevenshop.entity.ProductSpecTemplate;
import com.sevenshop.service.ProductSpecTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spec-templates")
@CrossOrigin(origins = "*")
public class ProductSpecTemplateController {

    @Autowired
    private ProductSpecTemplateService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTemplates() {
        List<Map<String, Object>> templates = service.getGroupedTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProductSpecTemplate>>> getAllTemplates() {
        List<ProductSpecTemplate> templates = service.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTemplate(@RequestBody ProductSpecTemplate template) {
        service.createTemplate(template);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateTemplate(@PathVariable Long id, @RequestBody ProductSpecTemplate template) {
        template.setId(id);
        service.updateTemplate(template);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
