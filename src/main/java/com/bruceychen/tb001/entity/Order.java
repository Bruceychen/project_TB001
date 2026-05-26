package com.bruceychen.tb001.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Order -- many-to-one to {@link User} and {@link Product}.
 *
 * <p>Table name is {@code orders} (not {@code order}) -- {@code order} is a SQL
 * reserved word; using the plural form avoids Hibernate having to quote it.
 *
 * <p>{@code totalCost} (order_amount * unit_price * (1 + tax_rate)) is NOT
 * persisted here -- it is computed at response time by the service / DTO layer.
 * Keeping derived data out of the entity avoids stale-cache and update-anomaly
 * bugs.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "order_amount", nullable = false)
    private Integer orderAmount;

    protected Order() {
        // JPA
    }

    public Order(User user, Product product, Integer orderAmount) {
        this.user = user;
        this.product = product;
        this.orderAmount = orderAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Integer orderAmount) {
        this.orderAmount = orderAmount;
    }
}
