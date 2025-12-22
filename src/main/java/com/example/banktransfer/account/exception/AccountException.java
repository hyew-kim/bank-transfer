package com.example.banktransfer.account.exception;

import com.example.banktransfer.global.exception.BusinessException;
import com.example.banktransfer.global.exception.ErrorCode;

public abstract class AccountException extends BusinessException {
  protected AccountException(String message) { super(message); }
  protected AccountException(String message, Throwable cause) { super(message, cause); }

  public static class LinkingInProgressException extends AccountException {
      public LinkingInProgressException() {
          super(ErrorCode.LINKING_IN_PROGRESS.getMessage());
      }

      @Override
      public ErrorCode getErrorCode() {
          return ErrorCode.LINKING_IN_PROGRESS;
      }
  }
}
