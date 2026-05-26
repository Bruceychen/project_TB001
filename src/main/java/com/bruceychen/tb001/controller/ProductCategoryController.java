package com.bruceychen.tb001.controller;

import com.bruceychen.tb001.entity.ProductCategory;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import com.bruceychen.tb001.repository.ProductCategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class ProductCategoryController {

    private final ProductCategoryRepository categoryRepository;

    public ProductCategoryController(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public record CategoryView(
            Long categoryId,
            String categoryName,
            BigDecimal taxRate
    ) {
        static CategoryView from(ProductCategory c) {
            return new CategoryView(c.getCategoryId(), c.getCategoryName(), c.getTaxRate());
        }
    }

    @GetMapping
    public List<CategoryView> list() {
        return categoryRepository.findAll().stream()
                .map(CategoryView::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryView get(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(CategoryView::from)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
