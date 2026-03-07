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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertThrows(BadRequestException.class, () -> orderItemService.addItem(finalO.getId(), finalP.getId(), 1));

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

    @Test
    void shouldPreventOversellWhenTwoRequestsHitAtSameTime() throws Exception {
        // Arrange: product with only 1 in stock
        Product p = new Product();
        p.setName("Race Product");
        p.setSku("RACE-" + System.nanoTime());
        p.setPrice(BigDecimal.valueOf(10));
        p.setInventoryQty(1);
        p = productRepository.save(p);

        // Arrange: user 1 + order 1
        User u1 = new User();
        u1.setName("User One");
        u1.setEmail("race1+" + System.nanoTime() + "@example.com");
        u1 = userRepository.save(u1);

        Order o1 = new Order();
        o1.setStatus(OrderStatus.NEW);
        o1.setUser(u1);
        o1 = orderRepository.save(o1);

        // Arrange: user 2 + order 2
        User u2 = new User();
        u2.setName("User Two");
        u2.setEmail("race2+" + System.nanoTime() + "@example.com");
        u2 = userRepository.save(u2);

        Order o2 = new Order();
        o2.setStatus(OrderStatus.NEW);
        o2.setUser(u2);
        o2 = orderRepository.save(o2);

        long before = orderItemRepository.count();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Long productId = p.getId();
        Long order1Id = o1.getId();
        Long order2Id = o2.getId();

        Callable<String> task1 = () -> {
            ready.countDown();
            start.await();
            try {
                orderItemService.addItem(order1Id, productId, 1);
                return "SUCCESS";
            } catch (Exception e) {
                return e.getClass().getSimpleName();
            }
        };

        Callable<String> task2 = () -> {
            ready.countDown();
            start.await();
            try {
                orderItemService.addItem(order2Id, productId, 1);
                return "SUCCESS";
            } catch (Exception e) {
                return e.getClass().getSimpleName();
            }
        };

        Future<String> f1 = executor.submit(task1);
        Future<String> f2 = executor.submit(task2);

        ready.await();
        start.countDown();

        String r1 = f1.get();
        String r2 = f2.get();

        executor.shutdown();

        // Assert: exactly one succeeds
        long successCount = Stream.of(r1, r2)
                .filter("SUCCESS"::equals)
                .count();
        assertEquals(1, successCount);

        // Assert: exactly one fails due to inventory conflict
        assertTrue(
                (r1.equals("SUCCESS") && r2.equals("ConflictException")) ||
                        (r2.equals("SUCCESS") && r1.equals("ConflictException"))
        );

        // Assert: inventory ends at 0, not negative / oversold
        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals(0, updated.getInventoryQty());

        // Assert: only one new order item got created
        long after = orderItemRepository.count();
        assertEquals(before + 1, after);
    }

}