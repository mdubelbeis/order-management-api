package com.masondubelbeis.order_management_api.repository;

import com.masondubelbeis.order_management_api.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}