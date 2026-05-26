package com.bruceychen.tb001.service;

import com.bruceychen.tb001.dto.OrderCreateRequest;
import com.bruceychen.tb001.dto.OrderPatchRequest;
import com.bruceychen.tb001.dto.OrderResponse;
import com.bruceychen.tb001.entity.Order;
import com.bruceychen.tb001.entity.Product;
import com.bruceychen.tb001.entity.ProductCategory;
import com.bruceychen.tb001.entity.User;
import com.bruceychen.tb001.repository.OrderRepository;
import com.bruceychen.tb001.repository.ProductRepository;
import com.bruceychen.tb001.repository.UserRepository;
import com.bruceychen.tb001.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks OrderService orderService;

    private static User user(Long id, String name) {
        User u = new User(name);
        ReflectionTestUtils.setField(u, "userId", id);
        return u;
    }

    private static ProductCategory category(Long id, String name, String taxRate) {
        ProductCategory c = new ProductCategory(name, new BigDecimal(taxRate));
        ReflectionTestUtils.setField(c, "categoryId", id);
        return c;
    }

    private static Product product(Long id, ProductCategory cat, String price) {
        Product p = new Product(cat, new BigDecimal(price));
        ReflectionTestUtils.setField(p, "productId", id);
        return p;
    }

    private static Order order(Long id, User u, Product p, int amount) {
        Order o = new Order(u, p, amount);
        ReflectionTestUtils.setField(o, "orderId", id);
        return o;
    }

    @Test
    void totalCost_amountTimesUnitPriceTimesOnePlusTaxRate() {
        ProductCategory cat = category(1L, "Food", "0.0500");
        Product prod = product(1L, cat, "100.00");
        User u = user(1L, "alice");
        Order o = order(1L, u, prod, 3);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUser_UserId(1L)).thenReturn(List.of(o));

        OrderResponse resp = orderService.findByUserId(1L).get(0);

        assertThat(resp.totalCost()).isEqualByComparingTo("315.00");
        assertThat(resp.unitPrice()).isEqualByComparingTo("100.00");
        assertThat(resp.taxRate()).isEqualByComparingTo("0.0500");
        assertThat(resp.orderAmount()).isEqualTo(3);
    }

    @Test
    void totalCost_zeroTaxRate_equalsAmountTimesPrice() {
        ProductCategory cat = category(2L, "Books", "0.0000");
        Product prod = product(2L, cat, "320.00");
        User u = user(2L, "bob");
        Order o = order(2L, u, prod, 5);

        when(userRepository.existsById(2L)).thenReturn(true);
        when(orderRepository.findByUser_UserId(2L)).thenReturn(List.of(o));

        assertThat(orderService.findByUserId(2L).get(0).totalCost()).isEqualByComparingTo("1600.00");
    }

    @Test
    void totalCost_roundsToTwoDecimalsHalfUp() {
        ProductCategory cat = category(3L, "Misc", "0.0833");
        Product prod = product(3L, cat, "99.99");
        User u = user(3L, "carol");
        Order o = order(3L, u, prod, 1);

        when(userRepository.existsById(3L)).thenReturn(true);
        when(orderRepository.findByUser_UserId(3L)).thenReturn(List.of(o));

        assertThat(orderService.findByUserId(3L).get(0).totalCost()).isEqualByComparingTo("108.32");
    }

    @Test
    void findByUserId_returnsAllUserOrders() {
        ProductCategory cat = category(1L, "Food", "0.05");
        Product p1 = product(1L, cat, "10.00");
        Product p2 = product(2L, cat, "20.00");
        User u = user(1L, "alice");

        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUser_UserId(1L)).thenReturn(List.of(
                order(1L, u, p1, 1),
                order(2L, u, p2, 2)
        ));

        List<OrderResponse> orders = orderService.findByUserId(1L);

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(OrderResponse::orderId).containsExactly(1L, 2L);
    }

    @Test
    void findByUserId_userHasNoOrders_returnsEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUser_UserId(1L)).thenReturn(List.of());

        assertThat(orderService.findByUserId(1L)).isEmpty();
    }

    @Test
    void findByUserId_missingUser_throwsEntityNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.findByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void create_validRequest_savesAndReturns() {
        ProductCategory cat = category(1L, "Food", "0.05");
        Product prod = product(1L, cat, "50.00");
        User u = user(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findById(1L)).thenReturn(Optional.of(prod));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            ReflectionTestUtils.setField(o, "orderId", 42L);
            return o;
        });

        OrderResponse resp = orderService.create(new OrderCreateRequest(1L, 1L, 4));

        assertThat(resp.orderId()).isEqualTo(42L);
        assertThat(resp.totalCost()).isEqualByComparingTo("210.00");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_missingUser_throwsAndDoesNotSave() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(new OrderCreateRequest(99L, 1L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_missingProduct_throwsAndDoesNotSave() {
        User u = user(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(new OrderCreateRequest(1L, 99L, 1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void patch_updatesOrderAmountOnly() {
        ProductCategory cat = category(1L, "Food", "0.05");
        Product prod = product(1L, cat, "100.00");
        User u = user(1L, "alice");
        Order existing = order(5L, u, prod, 3);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));

        OrderResponse resp = orderService.patch(5L, new OrderPatchRequest(null, 10));

        assertThat(resp.orderAmount()).isEqualTo(10);
        assertThat(resp.productId()).isEqualTo(1L);
        assertThat(resp.totalCost()).isEqualByComparingTo("1050.00");
    }

    @Test
    void patch_updatesProductOnly() {
        ProductCategory cat = category(1L, "Food", "0.05");
        Product oldProd = product(1L, cat, "100.00");
        Product newProd = product(2L, cat, "250.00");
        User u = user(1L, "alice");
        Order existing = order(5L, u, oldProd, 3);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProd));

        OrderResponse resp = orderService.patch(5L, new OrderPatchRequest(2L, null));

        assertThat(resp.productId()).isEqualTo(2L);
        assertThat(resp.orderAmount()).isEqualTo(3);
        assertThat(resp.totalCost()).isEqualByComparingTo("787.50");
    }

    @Test
    void patch_missingOrder_throwsEntityNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.patch(99L, new OrderPatchRequest(null, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order");
    }

    @Test
    void patch_missingProduct_throwsEntityNotFound() {
        ProductCategory cat = category(1L, "Food", "0.05");
        Product prod = product(1L, cat, "100.00");
        User u = user(1L, "alice");
        Order existing = order(5L, u, prod, 3);

        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.patch(5L, new OrderPatchRequest(99L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void delete_existingOrder_callsDeleteById() {
        when(orderRepository.existsById(5L)).thenReturn(true);

        orderService.delete(5L);

        verify(orderRepository).deleteById(5L);
    }

    @Test
    void delete_missingOrder_throwsAndDoesNotDelete() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).deleteById(any());
    }
}
