package com.hsbc.transaction_demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    @NotBlank(message = "Account number cannot be empty")
    private String accountNumber;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotNull(message = "Balance cannot be null")
    @Positive(message = "Balance must be greater than 0")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal balance;

    private LocalDateTime createdAt;
} 