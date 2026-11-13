package com.moneytrasnfer.moneytransferservice.exception;

import org.springframework.http.HttpStatus;

public enum FunctionalErrorCode {
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User with this unique ID already exists", HttpStatus.UNPROCESSABLE_CONTENT),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Account not found", HttpStatus.NOT_FOUND),
    SOURCE_ACCOUNT_NOT_FOUND("SOURCE_ACCOUNT_NOT_FOUND", "Source account not found", HttpStatus.NOT_FOUND),
    TARGET_ACCOUNT_NOT_FOUND("TARGET_ACCOUNT_NOT_FOUND", "Target account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS_FOR_CURRENCY("ACCOUNT_ALREADY_EXISTS_FOR_CURRENCY", "User already has an account with this currency type", HttpStatus.UNPROCESSABLE_CONTENT),
    SAME_ACCOUNT_TRANSFER("SAME_ACCOUNT_TRANSFER", "Source and target accounts must be different", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT("INVALID_AMOUNT", "Transfer amount must be greater than zero", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient balance in source account", HttpStatus.BAD_REQUEST),
    SINGLE_TRANSFER_LIMIT_EXCEEDED("SINGLE_TRANSFER_LIMIT_EXCEEDED", "Single transfer limit exceeded", HttpStatus.UNPROCESSABLE_CONTENT),
    DAILY_LIMIT_EXCEEDED("DAILY_LIMIT_EXCEEDED", "Daily limit exceeded", HttpStatus.UNPROCESSABLE_CONTENT),
    CROSS_CURRENCY_LIMIT_EXCEEDED("CROSS_CURRENCY_LIMIT_EXCEEDED", "Cross-currency limit exceeded", HttpStatus.UNPROCESSABLE_CONTENT),
    TRANSFER_RULE_VIOLATION("TRANSFER_RULE_VIOLATION", "Transfer Rules Violation", HttpStatus.UNPROCESSABLE_CONTENT);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    FunctionalErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
