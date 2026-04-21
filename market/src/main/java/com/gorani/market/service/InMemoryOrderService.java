package com.gorani.market.service;

import com.gorani.market.domain.MarketOrder;
import com.gorani.market.domain.OrderStatus;
import com.gorani.market.domain.Product;
import com.gorani.market.web.CheckoutForm;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryOrderService {
    private final AtomicLong sequence = new AtomicLong(1000L);
    private final Map<Long, MarketOrder> orderStore = new ConcurrentHashMap<>();
    private final Map<String, Long> externalOrderIndex = new ConcurrentHashMap<>();

    public MarketOrder createOrder(Product product, CheckoutForm form) {
        Long orderId = sequence.incrementAndGet();
        MarketOrder order = new MarketOrder(
                orderId,
                product,
                form.getQuantity(),
                form.getReceiverName(),
                form.getPhoneNumber(),
                form.getRoadAddress(),
                form.getDetailAddress(),
                form.getPaymentMethod()
        );
        orderStore.put(orderId, order);
        externalOrderIndex.put(order.getExternalOrderId(), orderId);
        // 주문 생성 로그
        System.out.println("[마켓] 주문 생성 완료 - 주문번호: " + orderId + ", 상품명: " + product.name());
        return order;
    }

    public MarketOrder findById(Long orderId) {
        MarketOrder order = orderStore.get(orderId);
        if (order == null) {
            throw new NoSuchElementException("주문을 찾을 수 없습니다. orderId=" + orderId);
        }
        return order;
    }

    public MarketOrder findByExternalOrderId(String externalOrderId) {
        Long orderId = externalOrderIndex.get(externalOrderId);
        if (orderId == null) {
            throw new NoSuchElementException("외부 주문번호를 찾을 수 없습니다. externalOrderId=" + externalOrderId);
        }
        return findById(orderId);
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        MarketOrder order = findById(orderId);
        order.updateStatus(status);
        // 주문 상태 변경 로그
        System.out.println("[마켓] 주문 상태 변경 - 주문번호: " + orderId + ", 상태: " + status);
    }
}
