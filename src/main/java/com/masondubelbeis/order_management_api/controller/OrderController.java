package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.User;
import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.UserRepository;
import com.masondubelbeis.order_management_api.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository,
                           UserRepository userRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @PostMapping("/user/{userId}")
    public Order createOrder(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);

        return orderRepository.save(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/{orderId}/checkout")
    public OrderResponse checkout(@PathVariable Long orderId) {
        return orderService.checkout(orderId);
    }
}