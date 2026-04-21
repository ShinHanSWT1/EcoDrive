package com.gorani.oilbank.service;

import com.gorani.oilbank.domain.OilbankCoupon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
public class OilbankCouponService {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ecodrive.base-url:http://localhost:8081}")
    private String ecodriveBaseUrl;

    @Value("${ecodrive.internal-token:local-dev-token}")
    private String ecodriveInternalToken;

    @Value("${gorani-pay.merchant-code:shinhan-oilbank}")
    private String merchantCode;

    public Optional<OilbankCoupon> findByCode(String couponCode, String externalOrderId) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }

        InternalCouponPreviewRequest request = new InternalCouponPreviewRequest(
                couponCode.trim(),
                merchantCode,
                externalOrderId
        );

        try {
            ResponseEntity<EcodriveApiResponse<InternalCouponPreviewResponse>> response = restTemplate.exchange(
                    ecodriveBaseUrl + "/api/internal/coupons/use-tokens/preview",
                    HttpMethod.POST,
                    new HttpEntity<>(request, buildHeaders()),
                    new ParameterizedTypeReference<>() {
                    }
            );
            EcodriveApiResponse<InternalCouponPreviewResponse> body = response.getBody();
            if (body == null || body.data() == null) {
                return Optional.empty();
            }
            InternalCouponPreviewResponse data = body.data();
            return Optional.of(new OilbankCoupon(data.oneTimeCode(), data.couponName(), data.discountAmount()));
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("[Oilbank] 쿠폰 검증 실패. couponCode={}, reason={}", couponCode, e.getResponseBodyAsString());
            return Optional.empty();
        }
    }

    public void consumeCoupon(String couponCode, String externalOrderId) {
        if (couponCode == null || couponCode.isBlank()) {
            return;
        }
        InternalCouponConsumeRequest request = new InternalCouponConsumeRequest(
                couponCode.trim(),
                merchantCode,
                externalOrderId
        );
        restTemplate.exchange(
                ecodriveBaseUrl + "/api/internal/coupons/use-tokens/consume",
                HttpMethod.POST,
                new HttpEntity<>(request, buildHeaders()),
                new ParameterizedTypeReference<EcodriveApiResponse<InternalCouponConsumeResponse>>() {
                }
        );
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(INTERNAL_TOKEN_HEADER, ecodriveInternalToken);
        return headers;
    }

    private record EcodriveApiResponse<T>(
            boolean success,
            String code,
            String message,
            T data
    ) {
    }

    private record InternalCouponPreviewRequest(
            String tokenCode,
            String merchantCode,
            String externalOrderId
    ) {
    }

    private record InternalCouponPreviewResponse(
            String oneTimeCode,
            Long userCouponId,
            String couponName,
            Integer discountAmount,
            String tokenExpiresAt
    ) {
    }

    private record InternalCouponConsumeRequest(
            String tokenCode,
            String merchantCode,
            String externalOrderId
    ) {
    }

    private record InternalCouponConsumeResponse(
            String oneTimeCode,
            Long userCouponId,
            String couponName,
            Integer discountAmount,
            String usedAt
    ) {
    }
}
