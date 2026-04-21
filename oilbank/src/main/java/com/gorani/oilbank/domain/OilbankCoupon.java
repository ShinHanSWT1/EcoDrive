package com.gorani.oilbank.domain;

public record OilbankCoupon(
        String code,
        String name,
        int discountAmount
) {
}

