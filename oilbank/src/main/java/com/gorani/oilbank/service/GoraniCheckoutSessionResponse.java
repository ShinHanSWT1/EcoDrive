package com.gorani.oilbank.service;

public record GoraniCheckoutSessionResponse(
        String sessionToken,
        String checkoutUrl,
        String status,
        String expiresAt
) {
}

