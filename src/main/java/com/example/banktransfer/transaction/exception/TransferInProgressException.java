package com.example.banktransfer.transaction.exception;

import com.example.banktransfer.global.exception.ErrorCode;

public class TransferInProgressException extends TransactionException {
    public TransferInProgressException() {
        super(ErrorCode.TRANSFER_IN_PROGRESS.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.TRANSFER_IN_PROGRESS;
    }
}
