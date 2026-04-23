package com.gorani.oilbank.service;

import com.gorani.oilbank.domain.OilbankOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GoraniPayCheckoutClient {
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gorani-pay.base-url:http://localhost:8083}")
    private String goraniPayBaseUrl;

    @Value("${gorani-pay.merchant-code:gorani-oilbank}")
    private String merchantCode;

    @Value("${gorani-pay.integration-type:PAY_LOGIN}")
    private String integrationType;

    @Value("${gorani-pay.internal-token:local-dev-token}")
    private String internalToken;

    public GoraniCheckoutSessionResponse createCheckoutSession(OilbankOrder order, String oilbankBaseUrl) {
        String successUrl = oilbankBaseUrl + "/payments/gorani/success";
        String failUrl = oilbankBaseUrl + "/payments/gorani/fail";

        GoraniCheckoutSessionRequest request = new GoraniCheckoutSessionRequest(
                merchantCode,
                null,
                order.getExternalOrderId(),
                order.getProduct().name(),
                order.getOriginalAmount(),
                0,
                order.getAppliedCouponAmount() == null ? 0 : order.getAppliedCouponAmount(),
                null,
                successUrl,
                failUrl,
                "MERCHANT_REDIRECT",
                "QR",
                integrationType
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(INTERNAL_TOKEN_HEADER, internalToken);

        log.info("[Oilbank] 고라니페이 세션 생성 요청. externalOrderId={}, originalAmount={}, couponDiscountAmount={}, finalAmount={}",
                order.getExternalOrderId(),
                order.getOriginalAmount(),
                order.getAppliedCouponAmount() == null ? 0 : order.getAppliedCouponAmount(),
                order.finalAmount());
        ResponseEntity<GoraniCheckoutSessionResponse> response = restTemplate.postForEntity(
                goraniPayBaseUrl + "/pay/checkout/sessions",
                new HttpEntity<>(request, headers),
                GoraniCheckoutSessionResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("怨좊씪?덊럹??checkout session ?앹꽦 ?ㅽ뙣");
        }
        return response.getBody();
    }
}
