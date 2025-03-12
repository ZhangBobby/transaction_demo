package com.hsbc.transaction_demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transaction_demo.dto.AccountDTO;
import com.hsbc.transaction_demo.exception.AccountException;
import com.hsbc.transaction_demo.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountDTO testAccount;

    @BeforeEach
    void setUp() {
        testAccount = AccountDTO.builder()
                .accountNumber("1234567890")
                .username("Test User")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void createAccount_ValidInput_ReturnsCreatedAccount() throws Exception {
        when(accountService.createAccount(any())).thenReturn(testAccount);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.username").value("Test User"))
                .andExpect(jsonPath("$.balance").value("1000.00"));
    }

    @Test
    void createAccount_InvalidInput_ReturnsBadRequest() throws Exception {
        AccountDTO invalidAccount = AccountDTO.builder()
                .accountNumber("")
                .username("")
                .balance(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAccount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_ExistingId_ReturnsAccount() throws Exception {
        when(accountService.getAccount("1234567890")).thenReturn(testAccount);

        mockMvc.perform(get("/api/accounts/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.username").value("Test User"))
                .andExpect(jsonPath("$.balance").value("1000.00"));
    }

    @Test
    void getAccount_NonExistingId_ReturnsNotFound() throws Exception {
        when(accountService.getAccount("9999999999"))
                .thenThrow(new AccountException("Account not found"));

        mockMvc.perform(get("/api/accounts/9999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAccounts_ReturnsListOfAccounts() throws Exception {
        AccountDTO anotherAccount = AccountDTO.builder()
                .accountNumber("0987654321")
                .username("Another User")
                .balance(new BigDecimal("2000.00"))
                .build();

        List<AccountDTO> accounts = Arrays.asList(testAccount, anotherAccount);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                .andExpect(jsonPath("$[0].balance").value("1000.00"))
                .andExpect(jsonPath("$[1].accountNumber").value("0987654321"))
                .andExpect(jsonPath("$[1].balance").value("2000.00"));
    }

    @Test
    void updateAccount_ValidInput_ReturnsUpdatedAccount() throws Exception {
        when(accountService.updateAccount(any(), any())).thenReturn(testAccount);

        mockMvc.perform(put("/api/accounts/1234567890")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.username").value("Test User"))
                .andExpect(jsonPath("$.balance").value("1000.00"));
    }

    @Test
    void updateAccount_NonExistingId_ReturnsNotFound() throws Exception {
        when(accountService.updateAccount(any(), any()))
                .thenThrow(new AccountException("Account not found"));

        mockMvc.perform(put("/api/accounts/9999999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAccount)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccount_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(accountService).deleteAccount("1234567890");

        mockMvc.perform(delete("/api/accounts/1234567890"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAccount_NonExistingId_ReturnsNotFound() throws Exception {
        doThrow(new AccountException("Account not found"))
                .when(accountService).deleteAccount("9999999999");

        mockMvc.perform(delete("/api/accounts/9999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBalance_ValidInput_ReturnsUpdatedAccount() throws Exception {
        when(accountService.updateBalance(any(), any())).thenReturn(testAccount);

        mockMvc.perform(patch("/api/accounts/1234567890/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content("1500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value("1000.00"));
    }

    @Test
    void updateBalance_InvalidAmount_ReturnsBadRequest() throws Exception {
        when(accountService.updateBalance(any(), any()))
                .thenThrow(new AccountException("Balance cannot be negative"));

        mockMvc.perform(patch("/api/accounts/1234567890/balance")
                .contentType(MediaType.APPLICATION_JSON)
                .content("-100.00"))
                .andExpect(status().isBadRequest());
    }
} 