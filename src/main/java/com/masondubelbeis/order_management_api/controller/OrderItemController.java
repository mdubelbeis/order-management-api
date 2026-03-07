package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public OrderResponse addItem(@RequestParam Long orderId,
                                 @RequestParam Long productId,
                                 @RequestParam Integer quantity) {
        return orderItemService.addItem(orderId, productId, quantity);
    }
}