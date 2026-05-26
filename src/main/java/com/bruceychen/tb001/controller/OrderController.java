package com.bruceychen.tb001.controller;

import com.bruceychen.tb001.dto.OrderCreateRequest;
import com.bruceychen.tb001.dto.OrderPatchRequest;
import com.bruceychen.tb001.dto.OrderResponse;
import com.bruceychen.tb001.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{userId}")
    public List<OrderResponse> getByUser(@PathVariable Long userId) {
        return orderService.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse created = orderService.create(request);
        return ResponseEntity
                .created(URI.create("/api/order/" + created.userId()))
                .body(created);
    }

    @PatchMapping("/{orderId}")
    public OrderResponse patch(@PathVariable Long orderId, @Valid @RequestBody OrderPatchRequest request) {
        return orderService.patch(orderId, request);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long orderId) {
        orderService.delete(orderId);
    }
}
