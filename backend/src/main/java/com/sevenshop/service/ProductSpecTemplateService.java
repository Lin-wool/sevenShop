package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sevenshop.entity.ProductSpecTemplate;
import com.sevenshop.mapper.ProductSpecTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductSpecTemplateService {

    @Autowired
    private ProductSpecTemplateMapper mapper;

    /**
     * 获取所有规格模板（按模板名称分组）
     */
    public List<Map<String, Object>> getGroupedTemplates() {
        List<ProductSpecTemplate> templates = mapper.selectList(
            new QueryWrapper<ProductSpecTemplate>().orderByAsc("name", "sort")
        );

        return templates.stream()
            .collect(Collectors.groupingBy(ProductSpecTemplate::getName))
            .entrySet().stream()
            .map(entry -> {
                List<Map<String, String>> specs = entry.getValue().stream()
                    .map(t -> Map.of(
                        "specName", t.getSpecName(),
                        "specValues", t.getSpecValues()
                    ))
                    .collect(Collectors.toList());
                return Map.of(
                    "name", entry.getKey(),
                    "specs", specs
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取所有规格模板（平铺列表）
     */
    public List<ProductSpecTemplate> getAllTemplates() {
        return mapper.selectList(
            new QueryWrapper<ProductSpecTemplate>().orderByAsc("name", "sort")
        );
    }

    /**
     * 创建规格模板
     */
    public void createTemplate(ProductSpecTemplate template) {
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new RuntimeException("模板名称不能为空");
        }
        if (template.getSpecName() == null || template.getSpecName().trim().isEmpty()) {
            throw new RuntimeException("规格名称不能为空");
        }
        if (template.getSpecValues() == null || template.getSpecValues().trim().isEmpty()) {
            throw new RuntimeException("规格值不能为空");
        }
        if (template.getSort() == null) {
            template.setSort(0);
        }
        mapper.insert(template);
    }

    /**
     * 更新规格模板
     */
    public void updateTemplate(ProductSpecTemplate template) {
        if (template.getId() == null) {
            throw new RuntimeException("ID不能为空");
        }
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new RuntimeException("模板名称不能为空");
        }
        if (template.getSpecName() == null || template.getSpecName().trim().isEmpty()) {
            throw new RuntimeException("规格名称不能为空");
        }
        if (template.getSpecValues() == null || template.getSpecValues().trim().isEmpty()) {
            throw new RuntimeException("规格值不能为空");
        }
        mapper.updateById(template);
    }

    /**
     * 删除规格模板
     */
    public void deleteTemplate(Long id) {
        mapper.deleteById(id);
    }
}
