package com.masondubelbeis.order_management_api.service;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderStatus;
import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.OrderItemRepository;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderResponse checkout(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.NEW) {
            throw new BadRequestException("Only NEW orders can be checked out. Current status: " + order.getStatus());
        }

        boolean hasItems = orderItemRepository.existsByOrderId(orderId);
        if (!hasItems) {
            throw new BadRequestException("Cannot checkout an empty order.");
        }

        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);

        return OrderResponse.from(saved); // see step 2
    }
}