package com.bruceychen.tb001.service;

import com.bruceychen.tb001.dto.OrderCreateRequest;
import com.bruceychen.tb001.dto.OrderPatchRequest;
import com.bruceychen.tb001.dto.OrderResponse;
import com.bruceychen.tb001.entity.Order;
import com.bruceychen.tb001.entity.Product;
import com.bruceychen.tb001.entity.User;
import com.bruceychen.tb001.event.OrderCreatedEvent;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import com.bruceychen.tb001.repository.OrderRepository;
import com.bruceychen.tb001.repository.ProductRepository;
import com.bruceychen.tb001.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return orderRepository.findByUser_UserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        Order order = new Order(user, product, request.orderAmount());
        Order saved = orderRepository.save(order);
        OrderResponse response = toResponse(saved);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                response.orderId(),
                response.userId(),
                response.username(),
                response.productId(),
                response.orderAmount(),
                response.totalCost()
        ));

        return response;
    }

    @Transactional
    public OrderResponse patch(Long orderId, OrderPatchRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (request.productId() != null) {
            Product product = productRepository.findById(request.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));
            order.setProduct(product);
        }
        if (request.orderAmount() != null) {
            order.setOrderAmount(request.orderAmount());
        }

        return toResponse(order);
    }

    @Transactional
    public void delete(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        orderRepository.deleteById(orderId);
    }

    private OrderResponse toResponse(Order order) {
        Product product = order.getProduct();
        BigDecimal unitPrice = product.getUnitPrice();
        BigDecimal taxRate = product.getCategory().getTaxRate();
        BigDecimal amount = BigDecimal.valueOf(order.getOrderAmount());
        BigDecimal totalCost = amount
                .multiply(unitPrice)
                .multiply(BigDecimal.ONE.add(taxRate))
                .setScale(2, RoundingMode.HALF_UP);

        return new OrderResponse(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getUser().getUsername(),
                product.getProductId(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(),
                unitPrice,
                taxRate,
                order.getOrderAmount(),
                totalCost
        );
    }
}
