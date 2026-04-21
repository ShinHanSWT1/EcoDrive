package com.gorani.market.service;

import com.gorani.market.domain.MarketOrder;
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
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Value("${gorani-pay.base-url:http://localhost:8083}")
    private String goraniPayBaseUrl;

    @Value("${gorani-pay.merchant-code:coopang-market}")
    private String merchantCode;

    @Value("${gorani-pay.integration-type:PAY_LOGIN}")
    private String integrationType;

    @Value("${gorani-pay.internal-token:local-dev-token}")
    private String internalToken;

    @Value("${market.base-url:http://localhost:9999}")
    private String marketBaseUrl;

    public GoraniCheckoutSessionResponse createCheckoutSession(MarketOrder order) {
        String successUrl = marketBaseUrl + "/payments/gorani/success";
        String failUrl = marketBaseUrl + "/payments/gorani/fail";

        GoraniCheckoutSessionRequest request = new GoraniCheckoutSessionRequest(
                merchantCode,
                null,
                order.getExternalOrderId(),
                order.getProduct().name(),
                order.getTotalAmount(),
                0,
                0,
                null,
                successUrl,
                failUrl,
                "MERCHANT_REDIRECT",
                "REDIRECT",
                integrationType
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(INTERNAL_TOKEN_HEADER, internalToken);

        log.info("[Market] 고라니페이 세션 생성 요청. externalOrderId={}, amount={}", order.getExternalOrderId(), order.getTotalAmount());
        ResponseEntity<GoraniCheckoutSessionResponse> response = restTemplate.postForEntity(
                goraniPayBaseUrl + "/pay/checkout/sessions",
                new HttpEntity<>(request, headers),
                GoraniCheckoutSessionResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("고라니페이 checkout session 생성 실패");
        }
        return response.getBody();
    }
}
