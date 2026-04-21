package com.gorani.market.domain;

public record Product(
        Long id,
        String name,
        String description,
        int price,
        String imageUrl
) {
}

