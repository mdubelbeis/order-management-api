package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.dto.CreateProductRequest;
import com.masondubelbeis.order_management_api.dto.ProductResponse;
import com.masondubelbeis.order_management_api.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody CreateProductRequest req) {
        return productService.create(req);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }
}