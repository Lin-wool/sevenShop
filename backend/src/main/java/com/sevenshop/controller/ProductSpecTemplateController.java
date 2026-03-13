package com.sevenshop.controller;

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
    public ResponseEntity<?> getTemplates() {
        List<Map<String, Object>> templates = service.getGroupedTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTemplates() {
        List<ProductSpecTemplate> templates = service.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody ProductSpecTemplate template) {
        try {
            service.createTemplate(template);
            return ResponseEntity.ok(Map.of("message", "创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @RequestBody ProductSpecTemplate template) {
        try {
            template.setId(id);
            service.updateTemplate(template);
            return ResponseEntity.ok(Map.of("message", "更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            service.deleteTemplate(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
