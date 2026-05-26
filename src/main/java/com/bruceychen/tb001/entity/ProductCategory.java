package com.bruceychen.tb001.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product category -- groups products and supplies the tax_rate used by the
 * {@code totalCost} formula.
 */
@Entity
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /** Tax rate as a decimal fraction, e.g. 0.05 = 5%. */
    @Column(name = "tax_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal taxRate;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    protected ProductCategory() {
        // JPA
    }

    public ProductCategory(String categoryName, BigDecimal taxRate) {
        this.categoryName = categoryName;
        this.taxRate = taxRate;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public List<Product> getProducts() {
        return products;
    }
}
