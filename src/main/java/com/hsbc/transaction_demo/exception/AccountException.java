package com.hsbc.transaction_demo.exception;

/**
 * 账户相关的异常类
 */
public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }
} 