package com.fooddelivery.user.application.rest;

import com.fooddelivery.user.application.dto.AuthResponse;
import com.fooddelivery.user.application.dto.LoginRequest;
import com.fooddelivery.user.application.dto.RegisterUserRequest;
import com.fooddelivery.user.application.dto.UserResponse;
import com.fooddelivery.user.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration, authentication, and profiles management")
public class UserController {

    private final UserApplicationService userApplicationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    @ApiResponse(responseCode = "201", description = "User successfully registered")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        log.info("Received registration request for email: {}", request.email());
        UserResponse response = userApplicationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user and generate JWT tokens")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Received login request for email: {}", request.email());
        AuthResponse response = userApplicationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        log.info("Received token refresh request");
        AuthResponse response = userApplicationService.refresh(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate refresh token")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    public ResponseEntity<Void> logout(@RequestHeader("X-Refresh-Token") String refreshToken) {
        log.info("Received logout request");
        userApplicationService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile details by ID")
    @ApiResponse(responseCode = "200", description = "User profile found")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        log.info("Received get user request for ID: {}", userId);
        UserResponse response = userApplicationService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
