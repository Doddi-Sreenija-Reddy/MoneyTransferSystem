package com.banking.transfer.controller;

import com.banking.transfer.config.SecurityConfig;
import com.banking.transfer.dto.AccountResponse;
import com.banking.transfer.dto.CreateAccountRequest;
import com.banking.transfer.dto.LoginRequest;
import com.banking.transfer.dto.TransactionResponse;
import com.banking.transfer.entity.AccountStatus;
import com.banking.transfer.entity.TransactionStatus;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.repository.AccountRepository;
import com.banking.transfer.repository.TransactionLogRepository;
import com.banking.transfer.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Focused web-layer tests for AccountController.
 * Uses MockMvc and a mocked AccountService to verify HTTP behavior, payload
 * shapes, status codes, and validation.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AccountService accountService;

        @MockBean
        private AccountRepository accountRepository;

        @MockBean
        private TransactionLogRepository transactionLogRepository;

        @Autowired
        private ObjectMapper objectMapper;

        // --- Test Data Builders (helpers) ---

        private CreateAccountRequest buildCreateAccountRequest() {
                return CreateAccountRequest.builder()
                                .username("alice")
                                .password("Secret@123")
                                .holderName("Alice Smith")
                                .initialBalance(new BigDecimal("1000.00"))
                                .build();
        }

        private LoginRequest buildLoginRequest() {
                return LoginRequest.builder()
                                .username("alice")
                                .password("Secret@123")
                                .build();
        }

        private AccountResponse buildAccountResponse() {
                return AccountResponse.builder()
                                .id("acc-123")
                                .username("alice")
                                .balance(new BigDecimal("1000.00"))
                                .status(AccountStatus.ACTIVE)
                                .build();
        }

        private TransactionResponse buildTxn(String id, String type, String fromId, String toId, String amount) {
                return TransactionResponse.builder()
                                .id(id)
                                .type(type) // e.g., "DEBIT"/"CREDIT"
                                .fromAccountId(fromId)
                                .toAccountId(toId)
                                .amount(new BigDecimal(amount))
                                .status(TransactionStatus.SUCCESS)
                                .createdOn(java.time.LocalDateTime.parse("2025-01-01T10:15:30"))
                                .build();
        }

        // --- Tests ---

        @Nested
        @DisplayName("POST /api/v1/accounts")
        class CreateAccount {

                @Test
                @DisplayName("should create account and return 201 with body")
                void createAccount_success() throws Exception {
                        var req = buildCreateAccountRequest();
                        var resp = buildAccountResponse();

                        Mockito.when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(resp);

                        mockMvc.perform(post("/api/v1/accounts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value("acc-123"))
                                        .andExpect(jsonPath("$.username").value("alice"))
                                        .andExpect(jsonPath("$.balance").value(1000.00))
                                        .andExpect(jsonPath("$.status").value("ACTIVE"));
                }

                @Test
                @DisplayName("should return 400 for validation errors (e.g., missing username)")
                void createAccount_validationError() throws Exception {
                        var invalid = CreateAccountRequest.builder()
                                        // missing username
                                        .password("Secret@123")
                                        .initialBalance(new BigDecimal("1000.00"))
                                        .build();

                        mockMvc.perform(post("/api/v1/accounts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalid)))
                                        .andExpect(status().isUnprocessableEntity());
                        // Optionally assert your GlobalExceptionHandler error format here if
                        // standardized
                }
        }

        @Nested
        @DisplayName("POST /api/v1/accounts/login")
        class Login {

                @Test
                @DisplayName("should login and return 200 with account response")
                void login_success() throws Exception {
                        var req = buildLoginRequest();
                        var resp = buildAccountResponse();

                        Mockito.when(accountService.login(any(LoginRequest.class))).thenReturn(resp);

                        mockMvc.perform(post("/api/v1/accounts/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value("acc-123"))
                                        .andExpect(jsonPath("$.username").value("alice"));
                }

                @Test
                @DisplayName("should return 400 for invalid login payload (validation)")
                void login_validationError() throws Exception {
                        var invalid = LoginRequest.builder()
                                        .username("") // invalid if @NotBlank
                                        .password("Secret@123")
                                        .build();

                        mockMvc.perform(post("/api/v1/accounts/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalid)))
                                        .andExpect(status().isUnprocessableEntity());
                }
        }

        @Nested
        @DisplayName("GET /api/v1/accounts/{id}")
        class GetAccount {

                @Test
                @DisplayName("should return account for given id")
                void getAccount_success() throws Exception {
                        var resp = buildAccountResponse();

                        Mockito.when(accountService.getAccountResponse(eq("acc-123"))).thenReturn(resp);

                        mockMvc.perform(get("/api/v1/accounts/{id}", "acc-123"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value("acc-123"))
                                        .andExpect(jsonPath("$.username").value("alice"));
                }

                @Test
                @DisplayName("should return 404 when account not found (handled by GlobalExceptionHandler)")
                void getAccount_notFound() throws Exception {
                        Mockito.when(accountService.getAccountResponse(eq("missing")))
                                        .thenThrow(new AccountNotFoundException("Account not found"));

                        mockMvc.perform(get("/api/v1/accounts/{id}", "missing"))
                                        .andExpect(status().isNotFound());
                        // Optionally assert error body format from GlobalExceptionHandler
                }
        }

        @Nested
        @DisplayName("GET /api/v1/accounts/{id}/balance")
        class GetBalance {

                @Test
                @DisplayName("should return account response (including balance)")
                void getBalance_success() throws Exception {
                        var resp = buildAccountResponse();

                        Mockito.when(accountService.getAccountResponse(eq("acc-123"))).thenReturn(resp);

                        mockMvc.perform(get("/api/v1/accounts/{id}/balance", "acc-123"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.id").value("acc-123"))
                                        .andExpect(jsonPath("$.balance").value(1000.00));
                }
        }

        @Nested
        @DisplayName("GET /api/v1/accounts/{id}/transactions")
        class GetTransactions {

                @Test
                @DisplayName("should return list of transactions for the account")
                void getTransactions_success() throws Exception {
                        var txns = List.of(
                                        buildTxn("tx-1", "DEBIT", "acc-123", "acc-456", "100.00"),
                                        buildTxn("tx-2", "CREDIT", "acc-789", "acc-123", "50.00"));

                        Mockito.when(accountService.getTransactions(eq("acc-123"))).thenReturn(txns);

                        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", "acc-123"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$", hasSize(2)))
                                        .andExpect(jsonPath("$[0].id").value("tx-1"))
                                        .andExpect(jsonPath("$[0].type").value("DEBIT"))
                                        .andExpect(jsonPath("$[0].amount").value(100.00))
                                        .andExpect(jsonPath("$[1].id").value("tx-2"))
                                        .andExpect(jsonPath("$[1].type").value("CREDIT"))
                                        .andExpect(jsonPath("$[1].amount").value(50.00));
                }
        }
}