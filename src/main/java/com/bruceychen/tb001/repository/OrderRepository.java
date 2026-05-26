package com.bruceychen.tb001.repository;

import com.bruceychen.tb001.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<Order> findByUser_UserId(Long userId);
}
