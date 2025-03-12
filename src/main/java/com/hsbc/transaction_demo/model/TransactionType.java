package com.hsbc.transaction_demo.model;

public enum TransactionType {
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 