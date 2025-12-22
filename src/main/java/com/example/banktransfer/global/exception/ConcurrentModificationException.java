package com.example.banktransfer.global.exception;

public class ConcurrentModificationException extends BusinessException {
    public ConcurrentModificationException() {
        super(ErrorCode.CONCURRENT_MODIFICATION.getMessage());
    }

    public ConcurrentModificationException(Throwable cause) {
        super(ErrorCode.CONCURRENT_MODIFICATION.getMessage(), cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.CONCURRENT_MODIFICATION;
    }
}
