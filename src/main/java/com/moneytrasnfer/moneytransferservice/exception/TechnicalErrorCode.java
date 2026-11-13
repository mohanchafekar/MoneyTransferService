package com.moneytrasnfer.moneytransferservice.exception;

import org.springframework.http.HttpStatus;

public enum TechnicalErrorCode {
    EXCHANGE_RATE_SERVICE_FAILURE("EXCHANGE_RATE_SERVICE_FAILURE", "Failed to fetch exchange rate from external service", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR("DATABASE_ERROR", "A database error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    TechnicalErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
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
