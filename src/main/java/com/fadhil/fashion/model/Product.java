package com.fadhil.fashion.model;

public record Product(
        String id,
        String name,
        String category,
        String tag,
        int price,
        String currency
) {
}
