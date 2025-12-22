package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class AccountAlreadyLinkedException extends AccountException {
    public AccountAlreadyLinkedException() {
        super(ErrorCode.ACCOUNT_ALREADY_LINKED.getMessage());
    }

    public AccountAlreadyLinkedException(Throwable cause) {
        super(ErrorCode.ACCOUNT_ALREADY_LINKED.getMessage(), cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ACCOUNT_ALREADY_LINKED;
    }
}
