package com.masondubelbeis.order_management_api.service;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderItem;
import com.masondubelbeis.order_management_api.domain.OrderStatus;
import com.masondubelbeis.order_management_api.domain.Product;
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
    public void addItem(Long orderId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Only allow items to be added to NEW orders
        if (order.getStatus() != null && order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Cannot add items to order in status: " + order.getStatus());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Integer current = product.getInventoryQty();
        int currentQty = (current == null) ? 0 : current;

        if (currentQty < quantity) {
            throw new IllegalStateException("Insufficient inventory. Have=" + currentQty + " requested=" + quantity);
        }

        // decrement inventory
        product.setInventoryQty(currentQty - quantity);
        productRepository.save(product);

        // create order item (price snapshot)
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(product.getPrice());

        orderItemRepository.save(item);
    }
}