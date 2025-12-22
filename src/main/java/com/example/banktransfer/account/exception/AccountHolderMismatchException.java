package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class AccountHolderMismatchException extends AccountException {
    public AccountHolderMismatchException() {
        super(ErrorCode.ACCOUNT_HOLDER_MISMATCH.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ACCOUNT_HOLDER_MISMATCH;
    }
}
