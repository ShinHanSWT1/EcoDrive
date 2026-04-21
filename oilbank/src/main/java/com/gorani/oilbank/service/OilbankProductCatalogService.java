package com.gorani.oilbank.service;

import com.gorani.oilbank.domain.OilbankProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OilbankProductCatalogService {
    private static final int GASOLINE_PRICE_PER_LITER = 1996;
    private static final int DIESEL_PRICE_PER_LITER = 1923;

    private final List<OilbankProduct> products = List.of(
            new OilbankProduct(1L, "주유", "휘발유", GASOLINE_PRICE_PER_LITER),
            new OilbankProduct(2L, "주유", "경유", DIESEL_PRICE_PER_LITER)
    );

    public List<OilbankProduct> findAll() {
        return products;
    }

    public OilbankProduct findById(Long productId) {
        return products.stream()
                .filter(product -> product.id().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }
}
