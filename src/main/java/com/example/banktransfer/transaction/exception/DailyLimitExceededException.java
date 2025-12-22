package com.example.banktransfer.transaction.exception;

import com.example.banktransfer.account.exception.AccountException;
import com.example.banktransfer.global.exception.ErrorCode;

public class DailyLimitExceededException extends AccountException {
    public DailyLimitExceededException() {
        super(ErrorCode.DAILY_LIMIT_EXCEEDED.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.DAILY_LIMIT_EXCEEDED;
    }
}
