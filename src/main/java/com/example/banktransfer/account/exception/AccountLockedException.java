package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class AccountLockedException extends AccountException {
    public AccountLockedException() {
        super(ErrorCode.ACCOUNT_LOCKED.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ACCOUNT_LOCKED;
    }
}
