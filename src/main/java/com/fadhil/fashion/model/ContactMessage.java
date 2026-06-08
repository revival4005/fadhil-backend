package com.fadhil.fashion.model;

public record ContactMessage(
        String id,
        String fullName,
        String phoneOrEmail,
        String topic,
        String message,
        String status,
        String createdAt
) {
}
