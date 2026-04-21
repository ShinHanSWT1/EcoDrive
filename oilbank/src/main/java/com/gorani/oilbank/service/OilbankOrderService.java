package com.gorani.oilbank.service;

import com.gorani.oilbank.domain.OilbankCoupon;
import com.gorani.oilbank.domain.OilbankOrder;
import com.gorani.oilbank.domain.OilbankPricingMode;
import com.gorani.oilbank.domain.OilbankProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class OilbankOrderService {
    private final AtomicLong sequence = new AtomicLong(2000);
    private final Map<Long, OilbankOrder> orders = new ConcurrentHashMap<>();
    private final Map<String, Long> externalOrderIdIndex = new ConcurrentHashMap<>();

    public OilbankOrder createOrder(
            OilbankProduct product,
            OilbankPricingMode pricingMode,
            Integer requestedLiters,
            Integer requestedAmount
    ) {
        long id = sequence.incrementAndGet();
        String externalOrderId = "OIL-" + id + "-" + System.currentTimeMillis();

        int originalAmount = switch (pricingMode) {
            case LITER -> requestedLiters * product.price();
            case AMOUNT -> requestedAmount;
        };

        OilbankOrder order = new OilbankOrder(
                id,
                externalOrderId,
                product,
                pricingMode,
                requestedLiters,
                requestedAmount,
                originalAmount
        );
        orders.put(id, order);
        externalOrderIdIndex.put(externalOrderId, id);
        log.info("[Oilbank] 주문 생성. orderId={}, externalOrderId={}, product={}, pricingMode={}, originalAmount={}",
                id, externalOrderId, product.name(), pricingMode, originalAmount);
        return order;
    }

    public List<OilbankOrder> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(OilbankOrder::getCreatedAt).reversed())
                .toList();
    }

    public OilbankOrder findById(Long orderId) {
        OilbankOrder order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
        }
        return order;
    }

    public OilbankOrder findByExternalOrderId(String externalOrderId) {
        Long orderId = externalOrderIdIndex.get(externalOrderId);
        if (orderId == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
        }
        return findById(orderId);
    }

    public void applyCoupon(Long orderId, OilbankCoupon coupon) {
        OilbankOrder order = findById(orderId);
        int discountAmount = Math.min(coupon.discountAmount(), order.getOriginalAmount());
        order.applyCoupon(coupon.code(), discountAmount);
        log.info("[Oilbank] 쿠폰 적용. orderId={}, couponCode={}, discountAmount={}, finalAmount={}",
                orderId, coupon.code(), discountAmount, order.finalAmount());
    }

    public void assignCheckout(Long orderId, String sessionToken, String checkoutUrl) {
        OilbankOrder order = findById(orderId);
        order.assignCheckout(sessionToken, checkoutUrl);
        log.info("[Oilbank] 결제 세션 연결. orderId={}, sessionToken={}", orderId, sessionToken);
    }

    public void markPaid(String externalOrderId, Long paymentId) {
        OilbankOrder order = findByExternalOrderId(externalOrderId);
        order.markPaid(paymentId);
        log.info("[Oilbank] 결제 완료. orderId={}, externalOrderId={}, paymentId={}",
                order.getId(), externalOrderId, paymentId);
    }

    public void markFailed(String externalOrderId, String message) {
        OilbankOrder order = findByExternalOrderId(externalOrderId);
        order.markFailed(message);
        log.warn("[Oilbank] 결제 실패. orderId={}, externalOrderId={}, reason={}",
                order.getId(), externalOrderId, message);
    }
}
