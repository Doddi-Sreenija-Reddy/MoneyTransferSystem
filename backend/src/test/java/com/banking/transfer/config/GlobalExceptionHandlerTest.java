package com.banking.transfer.config;

import com.banking.transfer.controller.AccountController;
import com.banking.transfer.controller.TransferController;
import com.banking.transfer.dto.AccountResponse;
import com.banking.transfer.dto.CreateAccountRequest;
import com.banking.transfer.dto.LoginRequest;
import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.exception.*;
import com.banking.transfer.service.AccountService;
import com.banking.transfer.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * End-to-end MVC tests that verify GlobalExceptionHandler mappings by invoking
 * real controller endpoints
 * and mocking service layer to throw exceptions handled by the advice.
 *
 * This ensures status codes and ErrorResponse payloads are correct.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        // Mock services used by controllers
        @MockBean
        private AccountService accountService;

        @MockBean
        private TransferService transferService;

        @MockBean
        private com.banking.transfer.repository.AccountRepository accountRepository;

        @MockBean
        private com.banking.transfer.repository.TransactionLogRepository transactionLogRepository;

        // ---- Helper DTO builders ----

        private CreateAccountRequest validCreateAccountRequest() {
                return CreateAccountRequest.builder()
                                .username("alice")
                                // strong password to satisfy common regex policies
                                .password("Secret@123A")
                                .holderName("Alice Smith")
                                .initialBalance(new BigDecimal("1000.00"))
                                .build();
        }

        private LoginRequest validLoginRequest() {
                return LoginRequest.builder()
                                .username("alice")
                                .password("Secret@123A")
                                .build();
        }

        private TransferRequest validTransferRequest() {
                return TransferRequest.builder()
                                .fromAccountId("acc-111")
                                .toAccountId("acc-222")
                                .amount(new BigDecimal("50.00"))
                                .idempotencyKey("ref-1")
                                .build();
        }

        private AccountResponse sampleAccountResponse() {
                return AccountResponse.builder()
                                .id("acc-1")
                                .username("alice")
                                .balance(new BigDecimal("1000.00"))
                                .status(com.banking.transfer.entity.AccountStatus.ACTIVE)
                                .build();
        }

        private TransferResponse sampleTransferResponse() {
                return TransferResponse.builder()
                                .transactionId("tx-1")
                                .debitedFrom("acc-111")
                                .creditedTo("acc-222")
                                .amount(new BigDecimal("50.00"))
                                .status("SUCCESS")

                                .message("ok")
                                .build();
        }

        // ---- Tests for specific exception mappings ----

        @Test
        @DisplayName("AccountNotFoundException -> 404, body contains errorCode ACC-404")
        void accountNotFound_404() throws Exception {
                Mockito.when(accountService.getAccountResponse(eq("missing")))
                                .thenThrow(new AccountNotFoundException("Account not found"));

                mockMvc.perform(get("/api/v1/accounts/{id}", "missing"))
                                .andExpect(status().isNotFound())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.errorCode").value("ACC-404"))
                                .andExpect(jsonPath("$.message", containsString("Account not found")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("AccountNotActiveException -> 403, body contains errorCode ACC-403")
        void accountNotActive_403() throws Exception {
                Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                .thenThrow(new AccountNotActiveException("Account is not active"));

                mockMvc.perform(post("/api/v1/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validTransferRequest())))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.errorCode").value("ACC-403"))
                                .andExpect(jsonPath("$.message", containsString("Account is not active")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("InsufficientBalanceException -> 400, body contains errorCode TRX-400")
        void insufficientBalance_400() throws Exception {
                Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

                mockMvc.perform(post("/api/v1/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validTransferRequest())))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value("TRX-400"))
                                .andExpect(jsonPath("$.message", containsString("Insufficient balance")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("DuplicateTransferException -> 409, body contains errorCode TRX-409")
        void duplicateTransfer_409() throws Exception {
                Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                .thenThrow(new DuplicateTransferException("Duplicate transfer"));

                mockMvc.perform(post("/api/v1/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validTransferRequest())))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.errorCode").value("TRX-409"))
                                .andExpect(jsonPath("$.message", containsString("Duplicate transfer")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("DuplicateUsernameException -> 409, body contains errorCode ACC-409")
        void duplicateUsername_409() throws Exception {
                Mockito.when(accountService.createAccount(any(CreateAccountRequest.class)))
                                .thenThrow(new DuplicateUsernameException("Username already exists"));

                mockMvc.perform(post("/api/v1/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validCreateAccountRequest())))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.errorCode").value("ACC-409"))
                                .andExpect(jsonPath("$.message", containsString("Username already exists")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("InvalidCredentialsException -> 401, body contains errorCode AUTH-401")
        void invalidCredentials_401() throws Exception {
                Mockito.when(accountService.login(any(LoginRequest.class)))
                                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

                mockMvc.perform(post("/api/v1/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginRequest())))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.errorCode").value("AUTH-401"))
                                .andExpect(jsonPath("$.message", containsString("Invalid credentials")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("IllegalArgumentException -> 422, body contains errorCode VAL-422")
        void illegalArgument_422() throws Exception {
                Mockito.when(accountService.getAccountResponse(eq("bad-input")))
                                .thenThrow(new IllegalArgumentException("Bad argument"));

                mockMvc.perform(get("/api/v1/accounts/{id}", "bad-input"))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.errorCode").value("VAL-422"))
                                .andExpect(jsonPath("$.message", containsString("Bad argument")))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("MethodArgumentNotValidException -> 422, body contains errorCode VAL-422 + aggregated messages")
        void methodArgumentNotValid_422() throws Exception {
                // Build an invalid payload to trigger @Valid failure in createAccount
                var invalid = CreateAccountRequest.builder()
                                // missing username to trigger @NotBlank, etc.
                                .password("short") // if your password requires strength, this may also contribute
                                .initialBalance(new BigDecimal("1000.00"))
                                .build();

                mockMvc.perform(post("/api/v1/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalid)))
                                .andExpect(status().isUnprocessableEntity())
                                .andExpect(jsonPath("$.errorCode").value("VAL-422"))
                                .andExpect(jsonPath("$.message", not(emptyString())))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Generic Exception -> 500, body contains errorCode SYS-500")
        void genericException_500() throws Exception {
                // Trigger via login flow; mock service to throw a RuntimeException not handled
                // specifically
                Mockito.when(accountService.login(any(LoginRequest.class)))
                                .thenThrow(new RuntimeException("Unexpected"));

                mockMvc.perform(post("/api/v1/accounts/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validLoginRequest())))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.errorCode").value("SYS-500"))
                                .andExpect(jsonPath("$.message").value("An internal error occurred"))
                                .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        // ---- Sanity test for success path (not strictly needed but helpful) ----

        @Nested
        @DisplayName("Sanity success responses (no exception)")
        class SuccessPaths {

                @Test
                @DisplayName("createAccount -> 201 CREATED with AccountResponse")
                void createAccount_success() throws Exception {
                        Mockito.when(accountService.createAccount(any(CreateAccountRequest.class)))
                                        .thenReturn(sampleAccountResponse());

                        mockMvc.perform(post("/api/v1/accounts")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validCreateAccountRequest())))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").value("acc-1"))
                                        .andExpect(jsonPath("$.username").value("alice"))
                                        .andExpect(jsonPath("$.status").value("ACTIVE"));
                }

                @Test
                @DisplayName("transfer -> 200 OK with TransferResponse")
                void transfer_success() throws Exception {
                        Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                        .thenReturn(sampleTransferResponse());

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(validTransferRequest())))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.status").value("SUCCESS"))
                                        .andExpect(jsonPath("$.debitedFrom").value("acc-111"))
                                        .andExpect(jsonPath("$.creditedTo").value("acc-222"))
                                        .andExpect(jsonPath("$.amount").value(50.00));
                }

                @Test
                @DisplayName("getTransactions -> 200 OK with array")
                void getTransactions_success() throws Exception {
                        Mockito.when(accountService.getTransactions(eq("acc-1")))
                                        .thenReturn(List.of());

                        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", "acc-1"))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
                }
        }
}