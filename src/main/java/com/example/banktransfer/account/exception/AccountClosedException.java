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

    public static class InvalidAccountException extends AccountException {
        public InvalidAccountException() {
            super(ErrorCode.ACCOUNT_NOT_FOUND.getMessage());
        }

        public InvalidAccountException(Long accountId) {
            super("계좌 ID %d는 존재하지 않습니다.".formatted(accountId));
        }

        @Override
        public ErrorCode getErrorCode() {
            return ErrorCode.ACCOUNT_NOT_FOUND;
        }
    }

    public static class InvalidAccountNumberException extends AccountException {
        public InvalidAccountNumberException() {
            super(ErrorCode.ACCOUNT_NUMBER_INVALID.getMessage());
        }

        @Override
        public ErrorCode getErrorCode() {
            return ErrorCode.ACCOUNT_NUMBER_INVALID;
        }
    }
}
