package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class AccountOwnershipException extends AccountException {
    public AccountOwnershipException() {
        super(ErrorCode.ACCOUNT_OWNERSHIP.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.ACCOUNT_OWNERSHIP;
    }

    public static class InsufficientBalanceException extends AccountException {
        public InsufficientBalanceException() {
            super(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
        }

        @Override
        public ErrorCode getErrorCode() {
            return ErrorCode.INSUFFICIENT_BALANCE;
        }
    }
}
