package com.gorani.oilbank.web;

import com.gorani.oilbank.domain.OilbankPricingMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateOrderForm {
    @NotNull
    private Long productId = 1L;

    @NotNull
    private OilbankPricingMode pricingMode = OilbankPricingMode.LITER;

    @Min(1)
    private int liters = 10;

    @Min(1)
    private int amountWon = 10000;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public OilbankPricingMode getPricingMode() {
        return pricingMode;
    }

    public void setPricingMode(OilbankPricingMode pricingMode) {
        this.pricingMode = pricingMode;
    }

    public int getLiters() {
        return liters;
    }

    public void setLiters(int liters) {
        this.liters = liters;
    }

    public int getAmountWon() {
        return amountWon;
    }

    public void setAmountWon(int amountWon) {
        this.amountWon = amountWon;
    }
}
