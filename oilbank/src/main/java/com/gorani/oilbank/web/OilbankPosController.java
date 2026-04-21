package com.gorani.oilbank.web;

import com.gorani.oilbank.domain.OilbankCoupon;
import com.gorani.oilbank.domain.OilbankOrder;
import com.gorani.oilbank.domain.OilbankOrderStatus;
import com.gorani.oilbank.domain.OilbankPricingMode;
import com.gorani.oilbank.service.GoraniCheckoutSessionResponse;
import com.gorani.oilbank.service.GoraniPayCheckoutClient;
import com.gorani.oilbank.service.OilbankCouponService;
import com.gorani.oilbank.service.OilbankOrderService;
import com.gorani.oilbank.service.OilbankProductCatalogService;
import com.gorani.oilbank.service.QrImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class OilbankPosController {
    private final OilbankProductCatalogService productCatalogService;
    private final OilbankOrderService orderService;
    private final OilbankCouponService couponService;
    private final GoraniPayCheckoutClient goraniPayCheckoutClient;
    private final QrImageService qrImageService;

    @Value("${oilbank.base-url:}")
    private String oilbankBaseUrl;

    @GetMapping("/")
    public String root() {
        return "redirect:/pos";
    }

    @GetMapping("/pos")
    public String pos(@RequestParam(required = false) Long orderId, Model model) {
        OilbankOrder selectedOrder = null;
        if (orderId != null) {
            selectedOrder = orderService.findById(orderId);
        }

        if (selectedOrder == null) {
            selectedOrder = orderService.findAll().stream().findFirst().orElse(null);
        }

        model.addAttribute("products", productCatalogService.findAll());
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("selectedOrder", selectedOrder);
        model.addAttribute("createOrderForm", new CreateOrderForm());
        model.addAttribute("couponApplyForm", new CouponApplyForm());
        model.addAttribute("pricingModes", OilbankPricingMode.values());
        return "pos";
    }

    @PostMapping("/pos/orders")
    public String createOrder(
            @Valid @ModelAttribute("createOrderForm") CreateOrderForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "주문 입력값 확인 필요");
            return "redirect:/pos";
        }

        OilbankPricingMode pricingMode = form.getPricingMode();
        Integer requestedLiters = null;
        Integer requestedAmount = null;

        if (pricingMode == OilbankPricingMode.LITER) {
            if (form.getLiters() < 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "리터 수량은 1L 이상 입력 필요");
                return "redirect:/pos";
            }
            requestedLiters = form.getLiters();
        } else {
            if (form.getAmountWon() < 1000) {
                redirectAttributes.addFlashAttribute("errorMessage", "금액은 1,000원 이상 입력 필요");
                return "redirect:/pos";
            }
            requestedAmount = form.getAmountWon();
        }

        OilbankOrder order = orderService.createOrder(
                productCatalogService.findById(form.getProductId()),
                pricingMode,
                requestedLiters,
                requestedAmount
        );
        redirectAttributes.addFlashAttribute("successMessage", "주문 생성 완료");
        return "redirect:/pos?orderId=" + order.getId();
    }

    @PostMapping("/pos/orders/{orderId}/coupon/apply")
    public String applyCoupon(
            @PathVariable Long orderId,
            @Valid @ModelAttribute("couponApplyForm") CouponApplyForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "쿠폰 코드 입력 필요");
            return "redirect:/pos?orderId=" + orderId;
        }

        OilbankOrder order = orderService.findById(orderId);
        if (order.getStatus() != OilbankOrderStatus.READY) {
            redirectAttributes.addFlashAttribute("errorMessage", "결제 진행 중 주문은 쿠폰 변경 불가");
            return "redirect:/pos?orderId=" + orderId;
        }

        OilbankCoupon coupon = couponService.findByCode(form.getCouponCode(), order.getExternalOrderId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드"));
        orderService.applyCoupon(orderId, coupon);
        redirectAttributes.addFlashAttribute("successMessage", "쿠폰 적용 완료");
        return "redirect:/pos?orderId=" + orderId;
    }

    @PostMapping("/pos/orders/{orderId}/pay/qr")
    public String issuePayQr(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        OilbankOrder order = orderService.findById(orderId);
        if (order.getStatus() == OilbankOrderStatus.PAID) {
            return "redirect:/pos?orderId=" + orderId;
        }

        String resolvedBaseUrl = resolveOilbankBaseUrl(request);
        GoraniCheckoutSessionResponse session = goraniPayCheckoutClient.createCheckoutSession(order, resolvedBaseUrl);
        orderService.assignCheckout(orderId, session.sessionToken(), session.checkoutUrl());
        return "redirect:" + session.checkoutUrl() + "/merchant-qr";
    }

    @GetMapping(value = "/pos/orders/{orderId}/qr-image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrImage(@PathVariable Long orderId) {
        OilbankOrder order = orderService.findById(orderId);
        if (order.getCheckoutUrl() == null || order.getCheckoutUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(qrImageService.generateQrPng(order.getCheckoutUrl(), 280, 280));
    }

    @GetMapping("/payments/gorani/success")
    public String paySuccess(
            @RequestParam String orderId,
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes
    ) {
        OilbankOrder order = orderService.findByExternalOrderId(orderId);
        orderService.markPaid(orderId, paymentId);
        log.info("[Oilbank] 성공 콜백 수신. externalOrderId={}, paymentId={}, status={}", orderId, paymentId, status);

        try {
            couponService.consumeCoupon(order.getAppliedCouponCode(), order.getExternalOrderId());
        } catch (Exception e) {
            log.warn("[Oilbank] 쿠폰 사용 확정 실패. externalOrderId={}, couponCode={}, reason={}",
                    order.getExternalOrderId(), order.getAppliedCouponCode(), e.getMessage());
        }

        redirectAttributes.addFlashAttribute("successMessage", "결제 완료");
        return "redirect:/pos?orderId=" + order.getId();
    }

    @GetMapping("/payments/gorani/fail")
    public String payFail(
            @RequestParam String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String message,
            RedirectAttributes redirectAttributes
    ) {
        orderService.markFailed(orderId, message);
        log.warn("[Oilbank] 실패 콜백 수신. externalOrderId={}, status={}, message={}", orderId, status, message);
        OilbankOrder order = orderService.findByExternalOrderId(orderId);
        redirectAttributes.addFlashAttribute("errorMessage", "결제 실패: " + (message == null ? "사유 미확인" : message));
        return "redirect:/pos?orderId=" + order.getId();
    }

    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> orderStatus(@PathVariable Long orderId) {
        OilbankOrder order = orderService.findById(orderId);
        return ResponseEntity.ok(Map.of(
                "orderId", String.valueOf(order.getId()),
                "externalOrderId", order.getExternalOrderId(),
                "status", order.getStatus().name()
        ));
    }

    private String resolveOilbankBaseUrl(HttpServletRequest request) {
        if (oilbankBaseUrl != null && !oilbankBaseUrl.isBlank()) {
            return oilbankBaseUrl;
        }
        return UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName())
                .port(request.getServerPort())
                .build()
                .toUriString();
    }
}
