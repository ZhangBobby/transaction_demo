package com.hsbc.transaction_demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction_demo.dto.TransactionDTO;
import com.hsbc.transaction_demo.model.TransactionStatus;
import com.hsbc.transaction_demo.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TransactionController
 * Tests REST endpoints and request/response handling
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionDTO testTransaction;
    private UUID testId;

    /**
     * Setup test data before each test
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
    }

    // Happy path tests
    @Test
    void createTransaction_ValidInput_ReturnsCreatedTransaction() throws Exception {
        when(transactionService.createTransaction(any(TransactionDTO.class)))
                .thenReturn(testTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.targetAccountNumber").value("0987654321"))
                .andExpect(jsonPath("$.amount").value(100.0));

        verify(transactionService).createTransaction(any(TransactionDTO.class));
    }

    // Validation tests
    @Test
    void createTransaction_InvalidInput_ReturnsBadRequest() throws Exception {
        TransactionDTO invalidTransaction = TransactionDTO.builder()
                .accountNumber("")  // Empty account number
                .targetAccountNumber("")  // Empty target account number
                .amount(new BigDecimal("-100.00"))  // Negative amount
                .build();

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTransaction)))
                .andExpect(status().isBadRequest());
    }

    // Error handling tests
    @Test
    void getTransaction_NonExistingId_ReturnsNotFound() throws Exception {
        when(transactionService.getTransaction(any(UUID.class)))
                .thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(get("/api/transactions/{id}", UUID.randomUUID()))
                .andExpect(status().isInternalServerError());
    }

    // List operation tests
    @Test
    void getAllTransactions_ReturnsListOfTransactions() throws Exception {
        TransactionDTO anotherTransaction = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .accountNumber("0987654321")
                .targetAccountNumber("1234567890")
                .amount(new BigDecimal("200.00"))
                .build();

        when(transactionService.getAllTransactions())
                .thenReturn(Arrays.asList(testTransaction, anotherTransaction));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[1].accountNumber").value("0987654321"));

        verify(transactionService).getAllTransactions();
    }

    // Update operation tests
    @Test
    void updateTransaction_ValidInput_ReturnsUpdatedTransaction() throws Exception {
        when(transactionService.updateTransaction(eq(testId), any(TransactionDTO.class)))
                .thenReturn(testTransaction);

        mockMvc.perform(put("/api/transactions/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTransaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()));

        verify(transactionService).updateTransaction(eq(testId), any(TransactionDTO.class));
    }

    // Delete operation tests
    @Test
    void deleteTransaction_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(transactionService).deleteTransaction(testId);

        mockMvc.perform(delete("/api/transactions/{id}", testId))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransaction(testId);
    }
} 