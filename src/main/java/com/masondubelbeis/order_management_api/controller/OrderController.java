package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/user/{userId}")
    public Order createOrder(@PathVariable Long userId) {
        return orderService.createOrder(userId);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/{orderId}/checkout")
    public OrderResponse checkout(@PathVariable Long orderId) {
        return orderService.checkout(orderId);
    }
}