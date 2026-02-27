package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderItem;
import com.masondubelbeis.order_management_api.domain.Product;
import com.masondubelbeis.order_management_api.repository.OrderItemRepository;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/order-items")
public class OrderItemController {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemController(OrderItemRepository orderItemRepository,
                               OrderRepository orderRepository,
                               ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @PostMapping
    public OrderItem addItem(@RequestParam Long orderId,
                             @RequestParam Long productId,
                             @RequestParam Integer quantity) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(product.getPrice());

        return orderItemRepository.save(item);
    }
}