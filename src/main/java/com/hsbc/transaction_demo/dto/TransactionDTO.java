package com.hsbc.transaction_demo.dto;

import com.hsbc.transaction_demo.model.TransactionStatus;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;

    @NotBlank(message = "Source account number cannot be empty")
    private String accountNumber;

    @NotBlank(message = "Target account number cannot be empty")
    private String targetAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
    private LocalDateTime timestamp;
    private TransactionStatus status;
} 