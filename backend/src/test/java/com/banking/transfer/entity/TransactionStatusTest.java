package com.banking.transfer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatusTest {

    @Test
    @DisplayName("should have SUCCESS status")
    void hasSuccessStatus() {
        TransactionStatus status = TransactionStatus.SUCCESS;
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("should have FAILED status")
    void hasFailedStatus() {
        TransactionStatus status = TransactionStatus.FAILED;
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("should have exactly 2 values")
    void hasTwoValues() {
        TransactionStatus[] values = TransactionStatus.values();
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(
                TransactionStatus.SUCCESS,
                TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("valueOf should return correct enum")
    void valueOf_returnsCorrectEnum() {
        assertThat(TransactionStatus.valueOf("SUCCESS")).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(TransactionStatus.valueOf("FAILED")).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("enum values should be comparable")
    void enumsAreComparable() {
        assertThat(TransactionStatus.SUCCESS).isNotEqualTo(TransactionStatus.FAILED);
        assertThat(TransactionStatus.SUCCESS).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(TransactionStatus.FAILED).isEqualTo(TransactionStatus.FAILED);
    }
}
