package com.bruceychen.tb001.repository;

import com.bruceychen.tb001.entity.Order;
import com.bruceychen.tb001.entity.Product;
import com.bruceychen.tb001.entity.ProductCategory;
import com.bruceychen.tb001.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class UserCascadeDeleteIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductCategoryRepository categoryRepository;
    @Autowired EntityManager em;

    @Test
    void deletingUser_removesAllUserOrders() {
        ProductCategory cat = categoryRepository.save(new ProductCategory("Food", new BigDecimal("0.0500")));
        Product product = productRepository.save(new Product(cat, new BigDecimal("100.00")));
        User user = new User("alice");
        Order o1 = new Order(user, product, 1);
        Order o2 = new Order(user, product, 2);
        user.getOrders().add(o1);
        user.getOrders().add(o2);
        userRepository.save(user);
        em.flush();

        Long userId = user.getUserId();
        assertThat(orderRepository.findByUser_UserId(userId)).hasSize(2);

        userRepository.deleteById(userId);
        em.flush();
        em.clear();

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(orderRepository.findByUser_UserId(userId)).isEmpty();
        assertThat(productRepository.findById(product.getProductId())).isPresent();
        assertThat(categoryRepository.findById(cat.getCategoryId())).isPresent();
    }

    @Test
    void deletingUser_doesNotAffectOtherUsersOrders() {
        ProductCategory cat = categoryRepository.save(new ProductCategory("Food", new BigDecimal("0.0500")));
        Product product = productRepository.save(new Product(cat, new BigDecimal("100.00")));

        User alice = new User("alice");
        alice.getOrders().add(new Order(alice, product, 1));
        userRepository.save(alice);

        User bob = new User("bob");
        bob.getOrders().add(new Order(bob, product, 5));
        userRepository.save(bob);
        em.flush();

        userRepository.deleteById(alice.getUserId());
        em.flush();
        em.clear();

        assertThat(orderRepository.findByUser_UserId(alice.getUserId())).isEmpty();
        assertThat(orderRepository.findByUser_UserId(bob.getUserId())).hasSize(1);
        assertThat(userRepository.findById(bob.getUserId())).isPresent();
    }
}
