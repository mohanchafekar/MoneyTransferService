package com.moneytrasnfer.moneytransferservice.exception;

import lombok.Getter;

@Getter
public class TechnicalException extends RuntimeException {

    private final TechnicalErrorCode errorCode;

    public TechnicalException(TechnicalErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public TechnicalException(TechnicalErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
