package com.masondubelbeis.order_management_api.service;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderItem;
import com.masondubelbeis.order_management_api.domain.OrderStatus;
import com.masondubelbeis.order_management_api.domain.Product;
import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.ConflictException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.OrderItemRepository;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderItemService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse addItem(Long orderId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("quantity must be > 0");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        requireStatus(order, OrderStatus.NEW, "add items to");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        Integer current = product.getInventoryQty();
        int currentQty = (current == null) ? 0 : current;

        if (currentQty < quantity) {
            throw new BadRequestException(
                    "Insufficient inventory. Have=" + currentQty + " requested=" + quantity
            );
        }

        product.setInventoryQty(currentQty - quantity);
        productRepository.save(product);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(product.getPrice());

        orderItemRepository.save(item);

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUserId(order.getUser().getId());
        return response;
    }

    private void requireStatus(Order order, OrderStatus expected, String action) {
        if (order.getStatus() != expected) {
            throw new BadRequestException("Cannot " + action + " order in status: " + order.getStatus());
        }
    }
}