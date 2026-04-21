package com.gorani.market.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketOrder {
    private final Long id;
    private final String externalOrderId;
    private final Product product;
    private final int quantity;
    private final int totalAmount;
    private final String receiverName;
    private final String phoneNumber;
    private final String roadAddress;
    private final String detailAddress;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime createdAt;
    private String goraniCheckoutUrl;
    private Long goraniPaymentId;
    private String failReason;
    private OrderStatus status;

    public MarketOrder(
            Long id,
            Product product,
            int quantity,
            String receiverName,
            String phoneNumber,
            String roadAddress,
            String detailAddress,
            PaymentMethod paymentMethod
    ) {
        this.id = id;
        this.externalOrderId = "MARKET-" + id + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.product = product;
        this.quantity = quantity;
        this.totalAmount = product.price() * quantity;
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.roadAddress = roadAddress;
        this.detailAddress = detailAddress;
        this.paymentMethod = paymentMethod;
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.READY;
    }

    public Long getId() {
        return id;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRoadAddress() {
        return roadAddress;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public synchronized String getGoraniCheckoutUrl() {
        return goraniCheckoutUrl;
    }

    public synchronized void updateGoraniCheckoutUrl(String goraniCheckoutUrl) {
        this.goraniCheckoutUrl = goraniCheckoutUrl;
    }

    public synchronized Long getGoraniPaymentId() {
        return goraniPaymentId;
    }

    public synchronized void updateGoraniPaymentId(Long goraniPaymentId) {
        this.goraniPaymentId = goraniPaymentId;
    }

    public synchronized String getFailReason() {
        return failReason;
    }

    public synchronized void updateFailReason(String failReason) {
        this.failReason = failReason;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
