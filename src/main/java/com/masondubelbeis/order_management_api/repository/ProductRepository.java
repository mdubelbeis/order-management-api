package com.masondubelbeis.order_management_api.repository;

import com.masondubelbeis.order_management_api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
}