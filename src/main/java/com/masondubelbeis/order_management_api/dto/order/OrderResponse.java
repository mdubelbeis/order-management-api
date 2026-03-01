package com.masondubelbeis.order_management_api.dto.order;

import com.masondubelbeis.order_management_api.domain.Order;
import com.masondubelbeis.order_management_api.domain.OrderStatus;

import java.time.Instant;

public class OrderResponse {

    private Long id;
    private OrderStatus status;
    private Instant createdAt;
    private Long userId;

    public static OrderResponse from(Order o) {
        OrderResponse dto = new OrderResponse();
        dto.setId(o.getId());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(Instant.from(o.getCreatedAt()));
        dto.setUserId(o.getUser().getId());
        return dto;
    }

    // getters/setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}