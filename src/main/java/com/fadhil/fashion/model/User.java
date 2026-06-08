package com.fadhil.fashion.model;

public record User(
        String id,
        String name,
        String email,
        String createdAt
) {
}
