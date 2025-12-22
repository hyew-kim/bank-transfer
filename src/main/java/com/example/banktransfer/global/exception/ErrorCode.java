package com.example.banktransfer.global.exception;

public enum ErrorCode {

    // Account
    ACCOUNT_NOT_FOUND(404, "계좌를 찾을 수 없습니다."),
    ACCOUNT_CLOSED(409, "이미 해지된 계좌입니다."),
    ACCOUNT_ALREADY_LINKED(409, "이미 등록된 계좌입니다."),
    LINKING_IN_PROGRESS(409, "계좌 등록이 진행 중입니다."),
    ACCOUNT_NUMBER_INVALID(400, "계좌번호 형식 오류"),
    ACCOUNT_HOLDER_MISMATCH(403, "예금주 불일치"),
    ACCOUNT_OWNERSHIP(403, "본인 계좌 아님"),
    ACCOUNT_LOCKED(409, "계좌 잠김"),
    INSUFFICIENT_BALANCE(400, "잔고가 부족합니다."),
    DAILY_LIMIT_EXCEEDED(400, "일일 한도 초과"),

    // Transaction
    TRANSFER_IN_PROGRESS(409, "이체 진행 중입니다."),

    // 공통
    CONCURRENT_MODIFICATION(409, "동시성 충돌"),
    UNAUTHORIZED(401, "권한이 없습니다."),
    INVALID_INPUT(400, "올바르지 않은 입력값 입니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
}
