package com.bruceychen.tb001.repository;

import com.bruceychen.tb001.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
