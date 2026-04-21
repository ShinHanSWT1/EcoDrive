package com.gorani.oilbank.service;

public record GoraniCheckoutSessionRequest(
        String merchantCode,
        String merchantUserKey,
        String externalOrderId,
        String title,
        Integer amount,
        Integer pointAmount,
        Integer couponDiscountAmount,
        Long payProductId,
        String successUrl,
        String failUrl,
        String entryMode,
        String channel,
        String integrationType
) {
}

