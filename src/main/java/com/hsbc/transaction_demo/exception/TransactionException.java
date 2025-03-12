package com.hsbc.transaction_demo.exception;

public class TransactionException extends RuntimeException {
    public TransactionException(String message) {
        super(message);
    }
} 