package com.masondubelbeis.order_management_api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderStatus;
import com.masondubelbeis.order_management_api.domain.Product;
import com.masondubelbeis.order_management_api.domain.User;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.ProductRepository;
import com.masondubelbeis.order_management_api.repository.UserRepository;
import com.masondubelbeis.order_management_api.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
class OrderItemIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldDecrementInventoryWhenItemAdded() {
        // Arrange: Product
        Product p = new Product();
        p.setName("Test");
        p.setSku("T-1");
        p.setPrice(BigDecimal.valueOf(10));
        p.setInventoryQty(10);
        p = productRepository.save(p);

        // Arrange: User (required by Order.user_id NOT NULL)
        User u = new User();
        u.setName("Test User");
        u.setEmail("test@example.com");
        u = userRepository.save(u);

        // Arrange: Order linked to User
        Order o = new Order();
        o.setStatus(OrderStatus.NEW);
        o.setUser(u);            // <-- THIS FIXES THE FAILURE
        o = orderRepository.save(o);

        // Act
        orderItemService.addItem(o.getId(), p.getId(), 2);

        // Assert
        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assertEquals(8, updated.getInventoryQty());
    }
}