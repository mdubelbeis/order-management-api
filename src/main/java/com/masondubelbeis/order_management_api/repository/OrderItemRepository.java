package com.masondubelbeis.order_management_api.repository;

import com.masondubelbeis.order_management_api.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderItemRepository extends JpaRepository<OrderItem, Long> { }