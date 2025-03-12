package com.hsbc.transaction_demo.service;

import com.hsbc.transaction_demo.dto.AccountDTO;
import com.hsbc.transaction_demo.dto.TransactionDTO;
import com.hsbc.transaction_demo.exception.TransactionException;
import com.hsbc.transaction_demo.model.Transaction;
import com.hsbc.transaction_demo.model.TransactionStatus;
import com.hsbc.transaction_demo.model.TransactionType;
import com.hsbc.transaction_demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountService accountService;

    public TransactionDTO createTransaction(TransactionDTO dto) {
        // Check for duplicate transactions (based on account, amount, type and timestamp)
        if (isDuplicateTransaction(dto)) {
            throw new TransactionException("Duplicate transaction detected");
        }

        // Verify source account exists and has sufficient balance
        AccountDTO sourceAccount = accountService.getAccount(dto.getAccountNumber());
        if (sourceAccount == null) {
            throw new TransactionException("Source account not found: " + dto.getAccountNumber());
        }
        
        // Verify target account exists
        AccountDTO targetAccount = accountService.getAccount(dto.getTargetAccountNumber());
        if (targetAccount == null) {
            throw new TransactionException("Target account not found: " + dto.getTargetAccountNumber());
        }

        // Check sufficient balance for debit transactions
        if (sourceAccount.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new TransactionException("Insufficient balance in source account");
        }

        // Update account balances
        accountService.updateBalance(sourceAccount.getAccountNumber(), sourceAccount.getBalance().subtract(dto.getAmount()));
        accountService.updateBalance(targetAccount.getAccountNumber(), targetAccount.getBalance().add(dto.getAmount()));

        Transaction transaction = convertToEntity(dto);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        Transaction saved = repository.save(transaction);
        return convertToDTO(saved);
    }

    public TransactionDTO getTransaction(UUID id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new TransactionException("Transaction not found with id: " + id));
    }

    public List<TransactionDTO> getAllTransactions() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return repository.findAll(pageable).map(this::convertToDTO);
    }

    public TransactionDTO updateTransaction(UUID id, TransactionDTO dto) {
        Transaction existing = repository.findById(id)
                .orElseThrow(() -> new TransactionException("Transaction not found with id: " + id));
        
        // Check status, if completed then modification is not allowed
        if (TransactionStatus.COMPLETED == existing.getStatus()) {
            throw new TransactionException("Cannot modify completed transaction");
        }
        
        existing.setAccountNumber(dto.getAccountNumber());
        existing.setTargetAccountNumber(dto.getTargetAccountNumber());
        existing.setAmount(dto.getAmount());
        existing.setDescription(dto.getDescription());
        
        Transaction updated = repository.save(existing);
        return convertToDTO(updated);
    }

    public void deleteTransaction(UUID id) {
        if (!repository.findById(id).isPresent()) {
            throw new TransactionException("Transaction not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private boolean isDuplicateTransaction(TransactionDTO dto) {
        return repository.findAll().stream()
                .anyMatch(t -> 
                    t.getAccountNumber().equals(dto.getAccountNumber()) &&
                    t.getAmount().equals(dto.getAmount()) &&
                    t.getTimestamp() != null &&
                    dto.getTimestamp() != null &&
                    Math.abs(t.getTimestamp().getMinute() - dto.getTimestamp().getMinute()) < 1
                );
    }

    private Transaction convertToEntity(TransactionDTO dto) {
        return Transaction.builder()
                .id(dto.getId())
                .accountNumber(dto.getAccountNumber())
                .targetAccountNumber(dto.getTargetAccountNumber())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .timestamp(dto.getTimestamp())
                .status(dto.getStatus())
                .build();
    }

    private TransactionDTO convertToDTO(Transaction entity) {
        return TransactionDTO.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .targetAccountNumber(entity.getTargetAccountNumber())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .timestamp(entity.getTimestamp())
                .status(entity.getStatus())
                .build();
    }
} 