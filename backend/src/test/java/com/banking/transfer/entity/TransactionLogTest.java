package com.banking.transfer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionLogTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder should create transaction log with all fields")
        void builder_allFields() {
            LocalDateTime now = LocalDateTime.now();
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-123")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .idempotencyKey("key-123")
                    .createdOn(now)
                    .build();

            assertThat(log.getId()).isEqualTo("TXN-123");
            assertThat(log.getFromAccountId()).isEqualTo("ACC-1");
            assertThat(log.getToAccountId()).isEqualTo("ACC-2");
            assertThat(log.getAmount()).isEqualByComparingTo("100.00");
            assertThat(log.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(log.getFailureReason()).isNull();
            assertThat(log.getIdempotencyKey()).isEqualTo("key-123");
            assertThat(log.getCreatedOn()).isEqualTo(now);
        }

        @Test
        @DisplayName("builder should create failed transaction with reason")
        void builder_failedTransaction() {
            TransactionLog log = TransactionLog.builder()
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("50.00"))
                    .status(TransactionStatus.FAILED)
                    .failureReason("Insufficient funds")
                    .idempotencyKey("key-456")
                    .build();

            assertThat(log.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(log.getFailureReason()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("setters should update all fields correctly")
        void setters_updateFields() {
            TransactionLog log = new TransactionLog();
            LocalDateTime now = LocalDateTime.now();

            log.setId("TXN-999");
            log.setFromAccountId("ACC-10");
            log.setToAccountId("ACC-20");
            log.setAmount(new BigDecimal("250.00"));
            log.setStatus(TransactionStatus.FAILED);
            log.setFailureReason("Account locked");
            log.setIdempotencyKey("key-999");
            log.setCreatedOn(now);

            assertThat(log.getId()).isEqualTo("TXN-999");
            assertThat(log.getFromAccountId()).isEqualTo("ACC-10");
            assertThat(log.getToAccountId()).isEqualTo("ACC-20");
            assertThat(log.getAmount()).isEqualByComparingTo("250.00");
            assertThat(log.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(log.getFailureReason()).isEqualTo("Account locked");
            assertThat(log.getIdempotencyKey()).isEqualTo("key-999");
            assertThat(log.getCreatedOn()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals should return true for same object")
        void equals_sameObject() {
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log).isEqualTo(log);
        }

        @Test
        @DisplayName("equals should return true for logs with same values")
        void equals_sameValues() {
            LocalDateTime now = LocalDateTime.now();
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(now)
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(now)
                    .build();

            assertThat(log1).isEqualTo(log2);
        }

        @Test
        @DisplayName("equals should return false for logs with different ids")
        void equals_differentIds() {
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-2")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-2")
                    .build();

            assertThat(log1).isNotEqualTo(log2);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equals_null() {
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should handle null id fields")
        void equals_nullIds() {
            TransactionLog log1 = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log1).isEqualTo(log2);
        }

        @Test
        @DisplayName("equals should return false when one id is null")
        void equals_oneNullId() {
            TransactionLog log1 = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log1).isNotEqualTo(log2);
        }

        @Test
        @DisplayName("equals should return false for different amounts")
        void equals_differentAmounts() {
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("200.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log1).isNotEqualTo(log2);
        }

        @Test
        @DisplayName("equals should return false for different statuses")
        void equals_differentStatuses() {
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.FAILED)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log1).isNotEqualTo(log2);
        }

        @Test
        @DisplayName("equals should handle null failureReason fields")
        void equals_nullFailureReasons() {
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .idempotencyKey("key-1")
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .idempotencyKey("key-1")
                    .build();

            assertThat(log1).isEqualTo(log2);
        }

        @Test
        @DisplayName("equals should handle null createdOn fields")
        void equals_nullCreatedOn() {
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(null)
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(null)
                    .build();

            assertThat(log1).isEqualTo(log2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCode_consistent() {
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .build();

            int hash1 = log.hashCode();
            int hash2 = log.hashCode();
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("hashCode should be equal for equal objects")
        void hashCode_equalObjects() {
            LocalDateTime now = LocalDateTime.now();
            TransactionLog log1 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(now)
                    .build();

            TransactionLog log2 = TransactionLog.builder()
                    .id("TXN-1")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-1")
                    .createdOn(now)
                    .build();

            assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
        }

        @Test
        @DisplayName("hashCode should handle null fields")
        void hashCode_nullFields() {
            TransactionLog log = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .idempotencyKey("key-1")
                    .createdOn(null)
                    .build();

            // Should not throw exception
            int hash = log.hashCode();
            assertThat(hash).isNotNull();
        }
    }

    @Nested
    @DisplayName("PrePersist Tests")
    class PrePersistTests {

        @Test
        @DisplayName("prePersist should set id and createdOn when null")
        void prePersist_setsFields() {
            TransactionLog log = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("acc-1")
                    .toAccountId("acc-2")
                    .amount(new BigDecimal("1.00"))
                    .status(TransactionStatus.SUCCESS)
                    .failureReason(null)
                    .idempotencyKey("unit-key")
                    .createdOn(null)
                    .build();

            log.prePersist();

            assertThat(log.getId()).isNotBlank();
            assertThat(log.getCreatedOn()).isNotNull();
        }

        @Test
        @DisplayName("prePersist should not override existing id")
        void prePersist_keepsExistingId() {
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-EXISTING")
                    .fromAccountId("acc-1")
                    .toAccountId("acc-2")
                    .amount(new BigDecimal("1.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("unit-key")
                    .createdOn(null)
                    .build();

            log.prePersist();

            assertThat(log.getId()).isEqualTo("TXN-EXISTING");
        }

        @Test
        @DisplayName("prePersist should not override existing createdOn")
        void prePersist_keepsExistingCreatedOn() {
            LocalDateTime existingTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            TransactionLog log = TransactionLog.builder()
                    .id(null)
                    .fromAccountId("acc-1")
                    .toAccountId("acc-2")
                    .amount(new BigDecimal("1.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("unit-key")
                    .createdOn(existingTime)
                    .build();

            log.prePersist();

            assertThat(log.getCreatedOn()).isEqualTo(existingTime);
        }
    }

    @Nested
    @DisplayName("ToString Test")
    class ToStringTest {

        @Test
        @DisplayName("toString should include key fields")
        void toString_includesFields() {
            TransactionLog log = TransactionLog.builder()
                    .id("TXN-123")
                    .fromAccountId("ACC-1")
                    .toAccountId("ACC-2")
                    .amount(new BigDecimal("100.00"))
                    .status(TransactionStatus.SUCCESS)
                    .idempotencyKey("key-123")
                    .build();

            String result = log.toString();
            assertThat(result).contains("TXN-123");
            assertThat(result).contains("ACC-1");
            assertThat(result).contains("ACC-2");
            assertThat(result).contains("SUCCESS");
        }
    }
}
