package com.gorani.oilbank.web;

import jakarta.validation.constraints.NotBlank;

public class CouponApplyForm {
    @NotBlank
    private String couponCode;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}

