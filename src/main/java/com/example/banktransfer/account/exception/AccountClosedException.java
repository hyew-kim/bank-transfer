package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class AccountClosedException extends AccountException {
    public AccountClosedException() {
        super(ErrorCode.ACCOUNT_CLOSED.getMessage());
    }

    public AccountClosedException(Long accountId) {
        super("계좌 ID %d는 이미 해지되었습니다.".formatted(accountId));
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ACCOUNT_CLOSED;
    }
}
