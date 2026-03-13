package com.sevenshop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sevenshop.entity.Category;
import com.sevenshop.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public Category createCategory(String name, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setSort(sort != null ? sort : 0);
        categoryMapper.insert(category);
        return category;
    }

    public Category updateCategory(Long id, String name, Integer sort) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        category.setName(name);
        if (sort != null) {
            category.setSort(sort);
        }
        categoryMapper.updateById(category);
        return category;
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return categoryMapper.selectList(wrapper);
    }

    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }
}
