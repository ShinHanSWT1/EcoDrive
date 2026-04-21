package com.gorani.market.service;

public record GoraniCheckoutSessionResponse(
        String sessionToken,
        String checkoutUrl,
        String status,
        String expiresAt
) {
}

