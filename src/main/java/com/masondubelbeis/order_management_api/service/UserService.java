package com.masondubelbeis.order_management_api.service;

import com.masondubelbeis.order_management_api.domain.User;
import com.masondubelbeis.order_management_api.dto.CreateUserRequest;
import com.masondubelbeis.order_management_api.dto.UserResponse;
import com.masondubelbeis.order_management_api.exception.BadRequestException;
import com.masondubelbeis.order_management_api.exception.NotFoundException;
import com.masondubelbeis.order_management_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(CreateUserRequest req) {
        userRepository.findByEmail(req.email()).ifPresent(u -> {
            throw new BadRequestException("Email already exists: " + req.email());
        });

        User user = new User();
        user.setEmail(req.email());
        user.setName(req.name());

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getCreatedAt());
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getName(), u.getCreatedAt()))
                .toList();
    }

    public User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}