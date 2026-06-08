package com.fadhil.fashion.model;

public record OrderRequest(
        String id,
        String fullName,
        String phoneOrEmail,
        String productRequest,
        String size,
        String delivery,
        String details,
        String status,
        String createdAt
) {
}
