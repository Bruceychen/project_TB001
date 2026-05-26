package com.bruceychen.tb001.controller;

import com.bruceychen.tb001.entity.Product;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import com.bruceychen.tb001.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public record ProductView(
            Long productId,
            Long categoryId,
            String categoryName,
            BigDecimal taxRate,
            BigDecimal unitPrice
    ) {
        static ProductView from(Product p) {
            return new ProductView(
                    p.getProductId(),
                    p.getCategory().getCategoryId(),
                    p.getCategory().getCategoryName(),
                    p.getCategory().getTaxRate(),
                    p.getUnitPrice()
            );
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProductView> list() {
        return productRepository.findAll().stream()
                .map(ProductView::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ProductView get(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ProductView::from)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
