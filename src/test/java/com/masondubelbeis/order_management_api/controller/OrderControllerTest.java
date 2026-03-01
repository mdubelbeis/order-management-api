package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.dto.order.OrderResponse;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.OrderRepository;
import com.masondubelbeis.order_management_api.repository.UserRepository;
import com.masondubelbeis.order_management_api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean OrderService orderService;

    // controller has these constructor deps, so we must provide them in the slice
    @MockitoBean UserRepository userRepository;
    @MockitoBean OrderRepository orderRepository;

    @Test
    void checkout_shouldReturn200_whenSuccessful() throws Exception {
        OrderResponse resp = new OrderResponse();
        resp.setId(10L);

        when(orderService.checkout(10L)).thenReturn(resp);

        mockMvc.perform(post("/orders/10/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void checkout_shouldReturn404_whenOrderNotFound() throws Exception {
        when(orderService.checkout(99L)).thenThrow(new NotFoundException("Order not found: 99"));

        mockMvc.perform(post("/orders/99/checkout"))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkout_shouldReturn400_whenInvalidState() throws Exception {
        when(orderService.checkout(11L)).thenThrow(new BadRequestException("Only NEW orders can be checked out."));

        mockMvc.perform(post("/orders/11/checkout"))
                .andExpect(status().isBadRequest());
    }
}