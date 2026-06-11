package com.banking.transfer.controller;

import com.banking.transfer.dto.TransferRequest;
import com.banking.transfer.dto.TransferResponse;
import com.banking.transfer.exception.AccountNotActiveException;
import com.banking.transfer.exception.AccountNotFoundException;
import com.banking.transfer.exception.DuplicateTransferException;
import com.banking.transfer.exception.InsufficientBalanceException;
import com.banking.transfer.repository.AccountRepository;
import com.banking.transfer.repository.TransactionLogRepository;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Focused MVC slice tests for TransferController.
 * Uses a mocked TransferService to verify HTTP status codes, payloads, and
 * validation behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // disable security filters for controller slice tests; remove if you want to
                                          // test security
class TransferControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private TransferService transferService;

        @MockBean
        private AccountRepository accountRepository;

        @MockBean
        private TransactionLogRepository transactionLogRepository;

        @Autowired
        private ObjectMapper objectMapper;

        // --- Helpers to build DTOs ---

        private TransferRequest buildValidRequest() {
                return TransferRequest.builder()
                                .fromAccountId("acc-111")
                                .toAccountId("acc-222")
                                .amount(new BigDecimal("150.00"))
                                .idempotencyKey("invoice-789")
                                .build();
        }

        private TransferResponse buildSuccessResponse() {
                return TransferResponse.builder()
                                .transactionId("tx-123")
                                .debitedFrom("acc-111")
                                .creditedTo("acc-222")
                                .amount(new BigDecimal("150.00"))
                                .status("SUCCESS")
                                .message("Transfer completed")
                                .build();
        }

        // --- Tests ---

        @Nested
        @DisplayName("POST /api/v1/transfers")
        class CreateTransfer {

                @Test
                @DisplayName("should perform transfer and return 200 OK with response body")
                void transfer_success() throws Exception {
                        var request = buildValidRequest();
                        var response = buildSuccessResponse();

                        Mockito.when(transferService.transfer(any(TransferRequest.class))).thenReturn(response);

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(jsonPath("$.transactionId", is("tx-123")))
                                        .andExpect(jsonPath("$.debitedFrom", is("acc-111")))
                                        .andExpect(jsonPath("$.creditedTo", is("acc-222")))
                                        .andExpect(jsonPath("$.amount", is(150.00)))
                                        .andExpect(jsonPath("$.status", is("SUCCESS")))
                                        .andExpect(jsonPath("$.message", is("Transfer completed")));
                }

                @Test
                @DisplayName("should return 400 Bad Request on validation errors (e.g., negative amount, missing fields)")
                void transfer_validationFailure() throws Exception {
                        // Example invalid request: missing fromAccountId and negative amount
                        var invalid = TransferRequest.builder()
                                        .toAccountId("acc-222")
                                        .amount(new BigDecimal("-1.00"))
                                        .idempotencyKey("bad-ref")
                                        .build();

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(invalid)))
                                        .andExpect(status().isUnprocessableEntity());
                        // If your GlobalExceptionHandler produces a specific error schema, assert it
                        // here.
                }

                @Test
                @DisplayName("should return 404 Not Found when account does not exist")
                void transfer_accountNotFound() throws Exception {
                        var req = buildValidRequest();
                        Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                        .thenThrow(new AccountNotFoundException("From or To account not found"));

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("should return 400 Bad Request when insufficient balance")
                void transfer_insufficientBalance() throws Exception {
                        var req = buildValidRequest();
                        Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                        .thenThrow(new InsufficientBalanceException("Insufficient balance"));

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("should return 409 Conflict on duplicate transfer")
                void transfer_duplicate() throws Exception {
                        var req = buildValidRequest();
                        Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                        .thenThrow(new DuplicateTransferException("Duplicate transfer detected"));

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isConflict());
                }

                @Test
                @DisplayName("should return 400 Bad Request when account is not active")
                void transfer_accountNotActive() throws Exception {
                        var req = buildValidRequest();
                        Mockito.when(transferService.transfer(any(TransferRequest.class)))
                                        .thenThrow(new AccountNotActiveException("Account is not active"));

                        mockMvc.perform(post("/api/v1/transfers")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(req)))
                                        .andExpect(status().isForbidden());
                }
        }
}