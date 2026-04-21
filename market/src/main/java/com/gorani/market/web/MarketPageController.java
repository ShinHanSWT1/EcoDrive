package com.gorani.market.web;

import com.gorani.market.domain.MarketOrder;
import com.gorani.market.domain.OrderStatus;
import com.gorani.market.domain.Product;
import com.gorani.market.service.GoraniCheckoutSessionResponse;
import com.gorani.market.service.GoraniPayCheckoutClient;
import com.gorani.market.service.InMemoryOrderService;
import com.gorani.market.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class MarketPageController {
    private final ProductCatalogService productCatalogService;
    private final InMemoryOrderService orderService;
    private final GoraniPayCheckoutClient goraniPayCheckoutClient;

    public MarketPageController(
            ProductCatalogService productCatalogService,
            InMemoryOrderService orderService,
            GoraniPayCheckoutClient goraniPayCheckoutClient
    ) {
        this.productCatalogService = productCatalogService;
        this.orderService = orderService;
        this.goraniPayCheckoutClient = goraniPayCheckoutClient;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("products", productCatalogService.findAll());
        return "products";
    }

    @GetMapping("/checkout/{productId}")
    public String checkoutPage(@PathVariable Long productId, Model model) {
        Product product = productCatalogService.findById(productId);
        model.addAttribute("form", new CheckoutForm());
        populateCheckoutModel(product, model);
        return "checkout";
    }

    @PostMapping("/checkout/{productId}")
    public String prepareCheckout(
            @PathVariable Long productId,
            @Valid @ModelAttribute("form") CheckoutForm form,
            BindingResult bindingResult,
            Model model
    ) {
        Product product = productCatalogService.findById(productId);
        if (bindingResult.hasErrors()) {
            populateCheckoutModel(product, model);
            return "checkout";
        }

        MarketOrder order = orderService.createOrder(product, form);
        GoraniCheckoutSessionResponse checkoutSession = goraniPayCheckoutClient.createCheckoutSession(order);
        order.updateGoraniCheckoutUrl(checkoutSession.checkoutUrl());
        orderService.updateOrderStatus(order.getId(), OrderStatus.PENDING);
        return "redirect:/checkout/ready/" + order.getId();
    }

    @GetMapping("/checkout/ready/{orderId}")
    public String checkoutReady(@PathVariable Long orderId, Model model) {
        MarketOrder order = orderService.findById(orderId);
        model.addAttribute("order", order);
        model.addAttribute("payCheckoutUrl", order.getGoraniCheckoutUrl());
        return "checkout-ready";
    }

    @GetMapping("/payments/gorani/success")
    public String goraniSuccessCallback(
            @RequestParam String orderId,
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) String status,
            Model model
    ) {
        MarketOrder order = orderService.findByExternalOrderId(orderId);
        order.updateGoraniPaymentId(paymentId);
        order.updateFailReason(null);
        orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

        // 결제 성공 콜백 로그
        System.out.println("[마켓] 고라니페이 결제 성공 콜백 - externalOrderId: " + orderId + ", paymentId: " + paymentId + ", status: " + status);

        model.addAttribute("order", order);
        model.addAttribute("status", OrderStatus.PAID);
        return "pay-complete";
    }

    @GetMapping("/payments/gorani/fail")
    public String goraniFailCallback(
            @RequestParam String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String message,
            Model model
    ) {
        MarketOrder order = orderService.findByExternalOrderId(orderId);
        order.updateFailReason(message);
        orderService.updateOrderStatus(order.getId(), OrderStatus.FAILED);

        // 결제 실패 콜백 로그
        System.out.println("[마켓] 고라니페이 결제 실패 콜백 - externalOrderId: " + orderId + ", status: " + status + ", message: " + message);

        model.addAttribute("order", order);
        model.addAttribute("status", OrderStatus.FAILED);
        return "pay-complete";
    }

    @GetMapping("/orders/{orderId}/result")
    public String orderResult(@PathVariable Long orderId, Model model) {
        MarketOrder order = orderService.findById(orderId);
        model.addAttribute("order", order);
        return "order-result";
    }

    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> orderStatus(@PathVariable Long orderId) {
        MarketOrder order = orderService.findById(orderId);
        return ResponseEntity.ok(Map.of(
                "orderId", String.valueOf(order.getId()),
                "status", order.getStatus().name()
        ));
    }

    @GetMapping("/error-page")
    public String errorPage() {
        return "error";
    }

    private void populateCheckoutModel(Product product, Model model) {
        Map<String, Object> user = Map.of(
                "name", "이예진",
                "address", "경기도 성남시 분당구 판교로 123",
                "phone", "010-0000-0000"
        );
        Map<String, Object> item = Map.of(
                "name", product.name(),
                "quantity", 1,
                "imageUrl", product.imageUrl()
        );
        Map<String, Object> order = Map.of(
                "user", user,
                "items", List.of(item)
        );
        List<Map<String, String>> bankList = List.of(
                Map.of("code", "088", "name", "신한은행"),
                Map.of("code", "004", "name", "국민은행"),
                Map.of("code", "020", "name", "우리은행")
        );

        // 결제 화면 모델 구성 로그
        System.out.println("[마켓] 결제 페이지 모델 구성 - 상품ID: " + product.id());

        model.addAttribute("product", product);
        model.addAttribute("order", order);
        model.addAttribute("bankList", bankList);
        model.addAttribute("totalPrice", product.price());
    }
}
