package com.example.banktransfer.transaction.exception;

import com.example.banktransfer.global.exception.BusinessException;

public abstract class TransactionException extends BusinessException {
  protected TransactionException(String message) { super(message); }
  protected TransactionException(String message, Throwable cause) { super(message, cause); }
}
