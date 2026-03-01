package com.masondubelbeis.order_management_api.controller;

import com.masondubelbeis.order_management_api.dto.CreateUserRequest;
import com.masondubelbeis.order_management_api.dto.UserResponse;
import com.masondubelbeis.order_management_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.create(req);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }
}