package com.gorani.oilbank.domain;

import java.time.LocalDateTime;

public class OilbankOrder {
    private final Long id;
    private final String externalOrderId;
    private final OilbankProduct product;
    private final OilbankPricingMode pricingMode;
    private final Integer requestedLiters;
    private final Integer requestedAmount;
    private final int originalAmount;
    private OilbankOrderStatus status;
    private Integer appliedCouponAmount;
    private String appliedCouponCode;
    private String checkoutUrl;
    private String checkoutSessionToken;
    private Long paymentId;
    private String failReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OilbankOrder(
            Long id,
            String externalOrderId,
            OilbankProduct product,
            OilbankPricingMode pricingMode,
            Integer requestedLiters,
            Integer requestedAmount,
            int originalAmount
    ) {
        this.id = id;
        this.externalOrderId = externalOrderId;
        this.product = product;
        this.pricingMode = pricingMode;
        this.requestedLiters = requestedLiters;
        this.requestedAmount = requestedAmount;
        this.originalAmount = originalAmount;
        this.status = OilbankOrderStatus.READY;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int finalAmount() {
        int discount = appliedCouponAmount == null ? 0 : appliedCouponAmount;
        return Math.max(originalAmount - discount, 0);
    }

    public void applyCoupon(String couponCode, int discountAmount) {
        this.appliedCouponCode = couponCode;
        this.appliedCouponAmount = discountAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignCheckout(String sessionToken, String checkoutUrl) {
        this.checkoutSessionToken = sessionToken;
        this.checkoutUrl = checkoutUrl;
        this.status = OilbankOrderStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaid(Long paymentId) {
        this.paymentId = paymentId;
        this.status = OilbankOrderStatus.PAID;
        this.failReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = OilbankOrderStatus.FAILED;
        this.failReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public OilbankProduct getProduct() {
        return product;
    }

    public OilbankPricingMode getPricingMode() {
        return pricingMode;
    }

    public Integer getRequestedLiters() {
        return requestedLiters;
    }

    public Integer getRequestedAmount() {
        return requestedAmount;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public OilbankOrderStatus getStatus() {
        return status;
    }

    public Integer getAppliedCouponAmount() {
        return appliedCouponAmount;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getCheckoutSessionToken() {
        return checkoutSessionToken;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getFailReason() {
        return failReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
