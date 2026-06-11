package com.banking.transfer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountStatusTest {

    @Test
    @DisplayName("should have ACTIVE status")
    void hasActiveStatus() {
        AccountStatus status = AccountStatus.ACTIVE;
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("should have LOCKED status")
    void hasLockedStatus() {
        AccountStatus status = AccountStatus.LOCKED;
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("LOCKED");
    }

    @Test
    @DisplayName("should have CLOSED status")
    void hasClosedStatus() {
        AccountStatus status = AccountStatus.CLOSED;
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("should have exactly 3 values")
    void hasThreeValues() {
        AccountStatus[] values = AccountStatus.values();
        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
                AccountStatus.ACTIVE,
                AccountStatus.LOCKED,
                AccountStatus.CLOSED);
    }

    @Test
    @DisplayName("valueOf should return correct enum")
    void valueOf_returnsCorrectEnum() {
        assertThat(AccountStatus.valueOf("ACTIVE")).isEqualTo(AccountStatus.ACTIVE);
        assertThat(AccountStatus.valueOf("LOCKED")).isEqualTo(AccountStatus.LOCKED);
        assertThat(AccountStatus.valueOf("CLOSED")).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    @DisplayName("enum values should be comparable")
    void enumsAreComparable() {
        assertThat(AccountStatus.ACTIVE).isNotEqualTo(AccountStatus.LOCKED);
        assertThat(AccountStatus.LOCKED).isNotEqualTo(AccountStatus.CLOSED);
        assertThat(AccountStatus.ACTIVE).isEqualTo(AccountStatus.ACTIVE);
    }
}
