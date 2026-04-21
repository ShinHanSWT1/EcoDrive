package com.gorani.market.web;

import com.gorani.market.domain.MarketOrder;
import com.gorani.market.domain.OrderStatus;
import com.gorani.market.service.InMemoryOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pay")
public class PayWindowController {
    private final InMemoryOrderService orderService;

    public PayWindowController(InMemoryOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/window/{orderId}")
    public String payWindow(@PathVariable Long orderId, Model model) {
        MarketOrder order = orderService.findById(orderId);
        model.addAttribute("order", order);
        return "pay-window";
    }

    @PostMapping("/window/{orderId}/complete")
    public String completePayment(
            @PathVariable Long orderId,
            @RequestParam String action,
            Model model
    ) {
        MarketOrder order = orderService.findById(orderId);
        OrderStatus nextStatus = switch (action) {
            case "success" -> OrderStatus.PAID;
            case "cancel" -> OrderStatus.CANCELED;
            default -> OrderStatus.FAILED;
        };
        orderService.updateOrderStatus(orderId, nextStatus);

        // 결제 처리 결과 로그
        System.out.println("[에코드라이브 페이] 결제 처리 완료 - 주문번호: " + orderId + ", 결과: " + nextStatus);

        model.addAttribute("order", order);
        model.addAttribute("status", nextStatus);
        return "pay-complete";
    }
}

