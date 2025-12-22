package com.example.banktransfer.global.exception;

public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
    public BusinessException(String message, Throwable cause) { super(message, cause); }

    // 클라이언트 응답용 공통 필드
    public abstract ErrorCode getErrorCode();
}
