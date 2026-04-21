package com.gorani.market.domain;

public enum PaymentMethod {
    ECODRIVE_PAY("EcoDrive Pay");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

