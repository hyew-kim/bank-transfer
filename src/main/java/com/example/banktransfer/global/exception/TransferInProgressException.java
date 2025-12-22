package com.example.banktransfer.global.exception;

import com.example.banktransfer.transaction.exception.TransactionException;

public class TransferInProgressException extends TransactionException {
    public TransferInProgressException() {
        super(ErrorCode.TRANSFER_IN_PROGRESS.getMessage());
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.TRANSFER_IN_PROGRESS;
    }
}
