package com.masondubelbeis.order_management_api.service;

import com.masondubelbeis.order_management_api.domain.Product;
import com.masondubelbeis.order_management_api.dto.CreateProductRequest;
import com.masondubelbeis.order_management_api.dto.ProductResponse;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(CreateProductRequest req) {
        productRepository.findBySku(req.sku()).ifPresent(p -> {
            throw new BadRequestException("SKU already exists: " + req.sku());
        });

        Product p = new Product();
        p.setSku(req.sku());
        p.setName(req.name());
        p.setPrice(req.price());
        p.setInventoryQty(req.inventoryQty());

        Product saved = productRepository.save(p);
        return new ProductResponse(saved.getId(), saved.getSku(), saved.getName(), saved.getPrice(), saved.getInventoryQty());
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(p -> new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getPrice(), p.getInventoryQty()))
                .toList();
    }

    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }
}