package com.example.banktransfer.transaction.exception;

import com.example.banktransfer.global.exception.BusinessException;
import com.example.banktransfer.global.exception.ErrorCode;

public class InvalidInputException extends BusinessException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_INPUT;
    }
}
