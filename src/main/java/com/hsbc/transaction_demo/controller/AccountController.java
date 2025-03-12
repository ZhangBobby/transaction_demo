package com.hsbc.transaction_demo.controller;

import com.hsbc.transaction_demo.dto.AccountDTO;
import com.hsbc.transaction_demo.exception.AccountException;
import com.hsbc.transaction_demo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Account Management API")
public class AccountController {
    private final AccountService service;

    @PostMapping
    @Operation(summary = "Create new account")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO account) {
        return ResponseEntity.status(201).body(service.createAccount(account));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(service.getAccount(accountNumber));
        } catch (AccountException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all accounts with pagination")
    public ResponseEntity<Page<AccountDTO>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "accountNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(service.getAllAccounts(pageRequest));
    }

    @PutMapping("/{accountNumber}")
    @Operation(summary = "Update account")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountDTO account) {
        try {
            return ResponseEntity.ok(service.updateAccount(accountNumber, account));
        } catch (AccountException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete account")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        try {
            service.deleteAccount(accountNumber);
            return ResponseEntity.noContent().build();
        } catch (AccountException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{accountNumber}/balance")
    @Operation(summary = "Update account balance")
    public ResponseEntity<AccountDTO> updateBalance(
            @PathVariable String accountNumber,
            @RequestBody BigDecimal balance) {
        try {
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(service.updateBalance(accountNumber, balance));
        } catch (AccountException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 