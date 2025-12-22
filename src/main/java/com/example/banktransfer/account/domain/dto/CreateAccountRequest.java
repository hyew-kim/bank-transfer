package com.example.banktransfer.account.domain.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateAccountRequest(
        @NotNull Long  userId,
        @Length(min = 3, max = 3, message = "은행코드는 3자리 입력하셔야 합니다.")
        @NotEmpty String bankCode,
        @NotEmpty
        @Length(min = 14, max = 14, message = "계좌번호는 14자리 입력하셔야 합니다.")
        String accountNumber,
        @NotEmpty String holderName
) {
    public static CreateAccountRequest of(Long userId, String bankCode, String accountNumber, String holderName) {
        return new CreateAccountRequest(userId, bankCode, accountNumber, holderName);
    }
}
