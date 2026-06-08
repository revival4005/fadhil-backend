package com.fadhil.fashion.controller;

import com.fadhil.fashion.model.ApiResponse;
import com.fadhil.fashion.model.ContactMessage;
import com.fadhil.fashion.model.LoginResponse;
import com.fadhil.fashion.model.OrderRequest;
import com.fadhil.fashion.model.Product;
import com.fadhil.fashion.model.User;
import com.fadhil.fashion.service.StoreService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ApiController {

    private final StoreService storeService;
    private final String apiBaseUrl;

    public ApiController(StoreService storeService, @Value("${app.api-base-url}") String apiBaseUrl) {
        this.storeService = storeService;
        this.apiBaseUrl = apiBaseUrl;
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "name", "FK Collection API",
                "status", "running",
                "baseUrl", "/api",
                "apiBaseUrl", apiBaseUrl
        );
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return storeService.health();
    }

    @GetMapping("/api/products")
    public ApiResponse products() {
        return ApiResponse.ok(storeService.products());
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse> register(@RequestBody Map<String, Object> body) {
        User user = storeService.register(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User registered successfully.", user));
    }

    @PostMapping("/api/auth/login")
    public ApiResponse login(@RequestBody Map<String, Object> body) {
        LoginResponse loginResponse = storeService.login(body);
        return ApiResponse.message("Logged in successfully.", loginResponse);
    }

    @PostMapping(value = "/api/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createOrder(@RequestBody Map<String, Object> body) {
        OrderRequest order = storeService.createOrder(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order request received.", order));
    }

    @PostMapping(value = "/api/orders", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse> createOrderFromForm(@RequestParam Map<String, Object> body) {
        OrderRequest order = storeService.createOrder(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order request received.", order));
    }

    @GetMapping("/api/orders")
    public ApiResponse orders() {
        return ApiResponse.ok(storeService.orders());
    }

    @PostMapping(value = "/api/contact", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createContactMessage(@RequestBody Map<String, Object> body) {
        ContactMessage message = storeService.createContactMessage(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Message received.", message));
    }

    @PostMapping(value = "/api/contact", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ApiResponse> createContactMessageFromForm(@RequestParam Map<String, Object> body) {
        ContactMessage message = storeService.createContactMessage(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Message received.", message));
    }

    @GetMapping("/api/contact")
    public ApiResponse contactMessages() {
        return ApiResponse.ok(storeService.contactMessages());
    }
}
