package com.hsbc.transaction_demo.controller;

import com.hsbc.transaction_demo.dto.TransactionDTO;
import com.hsbc.transaction_demo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "Transaction Management API")
public class TransactionController {
    private final TransactionService service;

    @PostMapping
    @Operation(summary = "Create new transaction")
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transaction) {
        return ResponseEntity.ok(service.createTransaction(transaction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getTransaction(id));
    }

    @GetMapping
    @Operation(summary = "Get all transactions with pagination")
    public ResponseEntity<Page<TransactionDTO>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(service.getAllTransactions(pageRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction")
    public ResponseEntity<TransactionDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionDTO transaction) {
        return ResponseEntity.ok(service.updateTransaction(id, transaction));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        service.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
} 