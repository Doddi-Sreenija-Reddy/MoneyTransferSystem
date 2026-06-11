package com.banking.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Holder name is required")
    private String holderName;

    /** Optional. When null, server defaults to 1000 for new accounts. */
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
    private BigDecimal initialBalance;
}
