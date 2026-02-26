package com.masondubelbeis.order_management_api.repository;

import com.masondubelbeis.order_management_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}