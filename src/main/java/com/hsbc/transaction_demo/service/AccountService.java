package com.hsbc.transaction_demo.service;

import com.hsbc.transaction_demo.dto.AccountDTO;
import com.hsbc.transaction_demo.exception.AccountException;
import com.hsbc.transaction_demo.model.Account;
import com.hsbc.transaction_demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;

    @Transactional
    public AccountDTO createAccount(AccountDTO accountDTO) {
        if (repository.findByAccountNumber(accountDTO.getAccountNumber()).isPresent()) {
            throw new AccountException("Account already exists: " + accountDTO.getAccountNumber());
        }

        if (accountDTO.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountException("Initial balance must be greater than or equal to 0");
        }

        Account account = Account.builder()
                .accountNumber(accountDTO.getAccountNumber())
                .username(accountDTO.getUsername())
                .balance(accountDTO.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        return convertToDTO(repository.save(account));
    }

    public AccountDTO getAccount(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .map(this::convertToDTO)
                .orElseThrow(() -> new AccountException("Account not found: " + accountNumber));
    }

    @Transactional
    public AccountDTO updateAccount(String accountNumber, AccountDTO accountDTO) {
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found: " + accountNumber));

        account.setUsername(accountDTO.getUsername());
        return convertToDTO(repository.save(account));
    }

    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found: " + accountNumber));

        repository.delete(account);
    }

    public List<AccountDTO> getAllAccounts() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountDTO updateBalance(String accountNumber, BigDecimal newBalance) {
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountException("Balance cannot be negative");
        }

        Account account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account not found: " + accountNumber));

        account.setBalance(newBalance);
        return convertToDTO(repository.save(account));
    }

    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                .accountNumber(account.getAccountNumber())
                .username(account.getUsername())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }
} 