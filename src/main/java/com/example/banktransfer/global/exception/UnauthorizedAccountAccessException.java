package com.example.banktransfer.global.exception;

import com.example.banktransfer.account.exception.AccountException;

public class UnauthorizedAccountAccessException extends AccountException {
    public UnauthorizedAccountAccessException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.UNAUTHORIZED;
    }
}
