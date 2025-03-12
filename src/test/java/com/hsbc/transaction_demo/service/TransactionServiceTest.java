package com.hsbc.transaction_demo.service;

import com.hsbc.transaction_demo.dto.AccountDTO;
import com.hsbc.transaction_demo.dto.TransactionDTO;
import com.hsbc.transaction_demo.exception.TransactionException;
import com.hsbc.transaction_demo.model.*;
import com.hsbc.transaction_demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService
 * Tests both successful operations and error scenarios
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService service;

    private TransactionDTO testTransaction;
    private AccountDTO sourceAccount;
    private AccountDTO targetAccount;
    private UUID testId;

    /**
     * Initialize test data before each test
     */
    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testTransaction = TransactionDTO.builder()
                .id(testId)
                .accountNumber("1234567890")
                .targetAccountNumber("0987654321")
                .amount(new BigDecimal("100.00"))
                .description("Test transaction")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        sourceAccount = AccountDTO.builder()
                .accountNumber("1234567890")
                .balance(new BigDecimal("1000.00"))
                .build();

        targetAccount = AccountDTO.builder()
                .accountNumber("0987654321")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    // Happy path tests
    @Test
    void createTransaction_Success() {
        when(accountService.getAccount(testTransaction.getAccountNumber())).thenReturn(sourceAccount);
        when(accountService.getAccount(testTransaction.getTargetAccountNumber())).thenReturn(targetAccount);
        when(repository.findAll()).thenReturn(Arrays.asList());
        when(repository.save(any())).thenReturn(Transaction.builder()
                .id(testId)
                .accountNumber(testTransaction.getAccountNumber())
                .targetAccountNumber(testTransaction.getTargetAccountNumber())
                .amount(testTransaction.getAmount())
                .timestamp(testTransaction.getTimestamp())
                .status(TransactionStatus.COMPLETED)
                .build());

        TransactionDTO result = service.createTransaction(testTransaction);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals("0987654321", result.getTargetAccountNumber());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        verify(repository).save(any());
        verify(accountService).updateBalance(eq("1234567890"), any(BigDecimal.class));
        verify(accountService).updateBalance(eq("0987654321"), any(BigDecimal.class));
    }

    @Test
    void getTransaction_Success() {
        when(repository.findById(testId)).thenReturn(Optional.of(Transaction.builder()
                .id(testId)
                .accountNumber(testTransaction.getAccountNumber())
                .targetAccountNumber(testTransaction.getTargetAccountNumber())
                .amount(testTransaction.getAmount())
                .timestamp(testTransaction.getTimestamp())
                .status(testTransaction.getStatus())
                .build()));

        TransactionDTO result = service.getTransaction(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals("0987654321", result.getTargetAccountNumber());
    }

    @Test
    void updateTransaction_Success() {
        Transaction existingTransaction = Transaction.builder()
                .id(testId)
                .accountNumber(testTransaction.getAccountNumber())
                .targetAccountNumber(testTransaction.getTargetAccountNumber())
                .amount(testTransaction.getAmount())
                .timestamp(testTransaction.getTimestamp())
                .status(TransactionStatus.PENDING)
                .build();

        when(repository.findById(testId)).thenReturn(Optional.of(existingTransaction));
        when(repository.save(any())).thenReturn(existingTransaction);

        TransactionDTO result = service.updateTransaction(testId, testTransaction);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals("0987654321", result.getTargetAccountNumber());
        verify(repository).save(any());
    }

    @Test
    void deleteTransaction_Success() {
        when(repository.findById(testId)).thenReturn(Optional.of(Transaction.builder()
                .id(testId)
                .status(TransactionStatus.PENDING)
                .build()));

        service.deleteTransaction(testId);

        verify(repository).deleteById(testId);
    }

    @Test
    void getAllTransactions_Success() {
        Transaction transaction1 = Transaction.builder()
                .id(testId)
                .accountNumber("1234567890")
                .targetAccountNumber("0987654321")
                .amount(new BigDecimal("100.00"))
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .accountNumber("0987654321")
                .targetAccountNumber("1234567890")
                .amount(new BigDecimal("200.00"))
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(transaction1, transaction2));

        var result = service.getAllTransactions();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testId, result.get(0).getId());
    }

    // Error scenario tests
    @Test
    void createTransaction_SourceAccountNotFound_ThrowsException() {
        when(accountService.getAccount(testTransaction.getAccountNumber())).thenReturn(null);

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.createTransaction(testTransaction));
        assertEquals("Source account not found: " + testTransaction.getAccountNumber(), exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void createTransaction_TargetAccountNotFound_ThrowsException() {
        when(accountService.getAccount(testTransaction.getAccountNumber())).thenReturn(sourceAccount);
        when(accountService.getAccount(testTransaction.getTargetAccountNumber())).thenReturn(null);

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.createTransaction(testTransaction));
        assertEquals("Target account not found: " + testTransaction.getTargetAccountNumber(), exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void createTransaction_InsufficientBalance_ThrowsException() {
        sourceAccount.setBalance(new BigDecimal("50.00")); // Set balance lower than transaction amount
        when(accountService.getAccount(testTransaction.getAccountNumber())).thenReturn(sourceAccount);
        when(accountService.getAccount(testTransaction.getTargetAccountNumber())).thenReturn(targetAccount);

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.createTransaction(testTransaction));
        assertEquals("Insufficient balance in source account", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void createTransaction_Duplicate_ThrowsException() {
        when(repository.findAll()).thenReturn(Arrays.asList(Transaction.builder()
                .accountNumber(testTransaction.getAccountNumber())
                .targetAccountNumber(testTransaction.getTargetAccountNumber())
                .amount(testTransaction.getAmount())
                .timestamp(testTransaction.getTimestamp())
                .build()));

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.createTransaction(testTransaction));
        assertEquals("Duplicate transaction detected", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void updateTransaction_Completed_ThrowsException() {
        when(repository.findById(testId)).thenReturn(Optional.of(Transaction.builder()
                .id(testId)
                .status(TransactionStatus.COMPLETED)
                .build()));

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.updateTransaction(testId, testTransaction));
        assertEquals("Cannot modify completed transaction", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void updateTransaction_NotFound_ThrowsException() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.updateTransaction(testId, testTransaction));
        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteTransaction_NotFound_ThrowsException() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.deleteTransaction(testId));
        assertTrue(exception.getMessage().contains("Transaction not found"));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void getTransaction_NotFound_ThrowsException() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        TransactionException exception = assertThrows(TransactionException.class,
                () -> service.getTransaction(testId));
        assertTrue(exception.getMessage().contains("Transaction not found"));
    }
} 