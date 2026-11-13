package com.moneytrasnfer.moneytransferservice.exception;

import lombok.Getter;

@Getter
public class FunctionalException extends RuntimeException {

    private final FunctionalErrorCode errorCode;

    public FunctionalException(FunctionalErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public FunctionalException(FunctionalErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
