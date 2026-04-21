package com.gorani.oilbank.domain;

public record OilbankProduct(
        Long id,
        String category,
        String name,
        int price
) {
}

