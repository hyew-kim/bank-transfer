package com.example.banktransfer.global.exception;

public class InvalidInputException extends BusinessException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT.getMessage());
    }

    public InvalidInputException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_INPUT;
    }
}
