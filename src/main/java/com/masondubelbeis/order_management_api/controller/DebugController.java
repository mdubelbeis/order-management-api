package com.masondubelbeis.order_management_api.controller;


import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {
    @GetMapping("/not-found")
    public void notFound() {
        throw new NotFoundException("Debug: not found");
    }

    @GetMapping("/bad-request")
    public void badRequest() {
        throw new BadRequestException("Debug: bad request");
    }
}
