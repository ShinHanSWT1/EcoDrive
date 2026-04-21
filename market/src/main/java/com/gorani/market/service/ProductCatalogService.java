package com.gorani.market.service;

import com.gorani.market.domain.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCatalogService {

    private final List<Product> products = List.of(
            new Product(1L, "친환경 타이어 세트", "저소음 고효율 타이어 4개 세트", 289000, "https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?w=900"),
            new Product(2L, "카본 절감 엔진오일", "연비 개선형 프리미엄 엔진오일", 59000, "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=900"),
            new Product(3L, "스마트 차량 공기청정기", "미세먼지 실시간 센싱 지원", 99000, "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=900")
    );

    public List<Product> findAll() {
        return products;
    }

    public Product findById(Long productId) {
        return products.stream()
                .filter(product -> product.id().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. id=" + productId));
    }
}

