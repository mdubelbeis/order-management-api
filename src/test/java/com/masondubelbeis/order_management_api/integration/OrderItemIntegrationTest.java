package com.masondubelbeis.order_management_api.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderStatus;
import com.masondubelbeis.order_management_api.domain.Product;
import com.masondubelbeis.order_management_api.domain.User;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.ConflictException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.ProductRepository;
import com.masondubelbeis.order_management_api.repository.UserRepository;
import com.masondubelbeis.order_management_api.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.masondubelbeis.order_management_api.repository.OrderItemRepository;

import java.math.BigDecimal;

@SpringBootTest
@Testcontainers
class OrderItemIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;


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
        u.setEmail("test+" + System.nanoTime() + "@example.com");
        u = userRepository.save(u);

        // Arrange: Order linked to User
        Order o = new Order();
        o.setStatus(OrderStatus.NEW);
        o.setUser(u);            // <-- THIS FIXES THE FAILURE
        o = orderRepository.save(o);

        long before = orderItemRepository.count();

        // Act
        orderItemService.addItem(o.getId(), p.getId(), 2);

        // Assert
        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assertEquals(8, updated.getInventoryQty());

        long after = orderItemRepository.count();
        assertEquals(before + 1, after);
    }

    @Test
    void shouldRejectWhenInsufficientInventory() {

        Product p = new Product();

        p.setName("Test");
        p.setSku("LOW-1");
        p.setPrice(BigDecimal.valueOf(10));
        p.setInventoryQty(1);
        p = productRepository.save(p);

        User u = new User();
        u.setName("Test User");
        u.setEmail("test+" + System.nanoTime() + "@example.com");
        u = userRepository.save(u);

        Order o = new Order();
        o.setStatus(OrderStatus.NEW);
        o.setUser(u);
        o = orderRepository.save(o);

        long before = orderItemRepository.count();

        Order finalO = o;
        Product finalP = p;
        assertThrows(ConflictException.class, () -> orderItemService.addItem(finalO.getId(), finalP.getId(), 2));

        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assertEquals(1, updated.getInventoryQty());

        long after = orderItemRepository.count();
        assertEquals(before, after);
    }

    @Test
    void shouldRejectWhenOrderIsNotNew() {
        Product p = new Product();
        p.setName("Test");
        p.setSku("STATUS-1");
        p.setPrice(BigDecimal.valueOf(10));
        p.setInventoryQty(10);
        p = productRepository.save(p);

        User u = new User();
        u.setName("Test User");
        u.setEmail("status+" + System.nanoTime() + "@example.com");
        u = userRepository.save(u);

        Order o = new Order();
        o.setStatus(OrderStatus.PAID); // not NEW
        o.setUser(u);
        o = orderRepository.save(o);

        long before = orderItemRepository.count();

        Order finalO = o;
        Product finalP = p;
        assertThrows(ConflictException.class, () -> orderItemService.addItem(finalO.getId(), finalP.getId(), 1));

        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assertEquals(10, updated.getInventoryQty());

        long after = orderItemRepository.count();
        assertEquals(before, after);
    }

    @Test
    void shouldRejectWhenQuantityIsZeroOrNegative() {
        // Arrange: Product
        Product p = new Product();
        p.setName("Test");
        p.setSku("QTY-1");
        p.setPrice(BigDecimal.valueOf(10));
        p.setInventoryQty(10);
        p = productRepository.save(p);

        // Arrange: User
        User u = new User();
        u.setName("Test User");
        u.setEmail("qty+" + System.nanoTime() + "@example.com");
        u = userRepository.save(u);

        // Arrange: Order
        Order o = new Order();
        o.setStatus(OrderStatus.NEW);
        o.setUser(u);
        o = orderRepository.save(o);

        // Act + Assert
        Order finalO = o;
        Product finalP = p;
        assertThrows(BadRequestException.class, () -> orderItemService.addItem(finalO.getId(), finalP.getId(), 0));
    }

    @Test
    void shouldThrowNotFoundWhenProductMissing() {
        // Arrange: User
        User u = new User();
        u.setName("Test User");
        u.setEmail("missing+" + System.nanoTime() + "@example.com");
        u = userRepository.save(u);

        // Arrange: Order
        Order o = new Order();
        o.setStatus(OrderStatus.NEW);
        o.setUser(u);
        o = orderRepository.save(o);

        // Act + Assert
        Order finalO = o;
        assertThrows(NotFoundException.class, () -> orderItemService.addItem(finalO.getId(), 999999L, 1));
    }

}