package com.banking.transfer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class AccountTest {

    private Account newAccount(BigDecimal balance, AccountStatus status) {
        return Account.builder()
                .id("ACC-TEST")
                .username("user1")
                .password("x")
                .holderName("User One")
                .balance(balance)
                .status(status)
                .version(0)
                .build();
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogic {

        @Test
        @DisplayName("debit should subtract when sufficient balance")
        void debit_success() {
            Account acc = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            acc.debit(new BigDecimal("40.00"));
            assertThat(acc.getBalance()).isEqualByComparingTo("60.00");
        }

        @Test
        @DisplayName("debit should throw IllegalArgumentException when insufficient balance")
        void debit_insufficient() {
            Account acc = newAccount(new BigDecimal("50.00"), AccountStatus.ACTIVE);
            assertThatThrownBy(() -> acc.debit(new BigDecimal("60.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance");
            assertThat(acc.getBalance()).isEqualByComparingTo("50.00"); // unchanged
        }

        @Test
        @DisplayName("debit should handle exact balance")
        void debit_exactBalance() {
            Account acc = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            acc.debit(new BigDecimal("100.00"));
            assertThat(acc.getBalance()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("credit should add amount to balance")
        void credit_success() {
            Account acc = newAccount(new BigDecimal("10.00"), AccountStatus.ACTIVE);
            acc.credit(new BigDecimal("2.50"));
            assertThat(acc.getBalance()).isEqualByComparingTo("12.50");
        }

        @Test
        @DisplayName("credit should handle large amounts")
        void credit_largeAmount() {
            Account acc = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            acc.credit(new BigDecimal("10000.00"));
            assertThat(acc.getBalance()).isEqualByComparingTo("10100.00");
        }

        @Test
        @DisplayName("isActive should return true for ACTIVE status")
        void isActive_whenActive() {
            Account active = newAccount(new BigDecimal("0.00"), AccountStatus.ACTIVE);
            assertThat(active.isActive()).isTrue();
        }

        @Test
        @DisplayName("isActive should return false for LOCKED status")
        void isActive_whenLocked() {
            Account locked = newAccount(new BigDecimal("0.00"), AccountStatus.LOCKED);
            assertThat(locked.isActive()).isFalse();
        }

        @Test
        @DisplayName("isActive should return false for CLOSED status")
        void isActive_whenClosed() {
            Account closed = newAccount(new BigDecimal("0.00"), AccountStatus.CLOSED);
            assertThat(closed.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder should create account with all fields")
        void builder_allFields() {
            LocalDateTime now = LocalDateTime.now();
            Account account = Account.builder()
                    .id("ACC-123")
                    .username("testuser")
                    .password("password123")
                    .holderName("Test User")
                    .balance(new BigDecimal("1000.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(1)
                    .lastUpdated(now)
                    .build();

            assertThat(account.getId()).isEqualTo("ACC-123");
            assertThat(account.getUsername()).isEqualTo("testuser");
            assertThat(account.getPassword()).isEqualTo("password123");
            assertThat(account.getHolderName()).isEqualTo("Test User");
            assertThat(account.getBalance()).isEqualByComparingTo("1000.00");
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(account.getVersion()).isEqualTo(1);
            assertThat(account.getLastUpdated()).isEqualTo(now);
        }

        @Test
        @DisplayName("builder should handle minimal fields")
        void builder_minimalFields() {
            Account account = Account.builder()
                    .username("user")
                    .password("pass")
                    .holderName("Name")
                    .balance(BigDecimal.ZERO)
                    .status(AccountStatus.ACTIVE)
                    .build();

            assertThat(account.getUsername()).isEqualTo("user");
            assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("setters should update all fields correctly")
        void setters_updateFields() {
            Account account = new Account();
            LocalDateTime now = LocalDateTime.now();

            account.setId("ACC-999");
            account.setUsername("newuser");
            account.setPassword("newpass");
            account.setHolderName("New Name");
            account.setBalance(new BigDecimal("500.00"));
            account.setStatus(AccountStatus.LOCKED);
            account.setVersion(5);
            account.setLastUpdated(now);

            assertThat(account.getId()).isEqualTo("ACC-999");
            assertThat(account.getUsername()).isEqualTo("newuser");
            assertThat(account.getPassword()).isEqualTo("newpass");
            assertThat(account.getHolderName()).isEqualTo("New Name");
            assertThat(account.getBalance()).isEqualByComparingTo("500.00");
            assertThat(account.getStatus()).isEqualTo(AccountStatus.LOCKED);
            assertThat(account.getVersion()).isEqualTo(5);
            assertThat(account.getLastUpdated()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals should return true for same object")
        void equals_sameObject() {
            Account account = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            assertThat(account).isEqualTo(account);
        }

        @Test
        @DisplayName("equals should return true for accounts with same values")
        void equals_sameValues() {
            Account account1 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            Account account2 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false for accounts with different ids")
        void equals_differentIds() {
            Account account1 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            account1.setId("ACC-1");
            Account account2 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            account2.setId("ACC-2");

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false for null")
        void equals_null() {
            Account account = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            assertThat(account).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals should return false for different class")
        void equals_differentClass() {
            Account account = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            assertThat(account).isNotEqualTo("not an account");
        }

        @Test
        @DisplayName("equals should handle null id fields")
        void equals_nullIds() {
            Account account1 = Account.builder()
                    .id(null)
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            Account account2 = Account.builder()
                    .id(null)
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false when one id is null")
        void equals_oneNullId() {
            Account account1 = Account.builder()
                    .id(null)
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            Account account2 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false for different usernames")
        void equals_differentUsernames() {
            Account account1 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            account1.setUsername("user1");
            Account account2 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            account2.setUsername("user2");

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false for different balances")
        void equals_differentBalances() {
            Account account1 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            Account account2 = newAccount(new BigDecimal("200.00"), AccountStatus.ACTIVE);

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("equals should return false for different statuses")
        void equals_differentStatuses() {
            Account account1 = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            Account account2 = newAccount(new BigDecimal("100.00"), AccountStatus.LOCKED);

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("equals should handle null version fields")
        void equals_nullVersions() {
            Account account1 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(null)
                    .build();

            Account account2 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(null)
                    .build();

            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("equals should handle null lastUpdated fields")
        void equals_nullLastUpdated() {
            Account account1 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .lastUpdated(null)
                    .build();

            Account account2 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .lastUpdated(null)
                    .build();

            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("hashCode should be consistent")
        void hashCode_consistent() {
            Account account = newAccount(new BigDecimal("100.00"), AccountStatus.ACTIVE);
            int hash1 = account.hashCode();
            int hash2 = account.hashCode();
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("hashCode should be equal for equal objects")
        void hashCode_equalObjects() {
            Account account1 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            Account account2 = Account.builder()
                    .id("ACC-1")
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(0)
                    .build();

            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("hashCode should handle null fields")
        void hashCode_nullFields() {
            Account account = Account.builder()
                    .id(null)
                    .username("user1")
                    .password("pass")
                    .holderName("Name")
                    .balance(new BigDecimal("100.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(null)
                    .lastUpdated(null)
                    .build();

            // Should not throw exception
            int hash = account.hashCode();
            assertThat(hash).isNotNull();
        }
    }

    @Nested
    @DisplayName("PrePersist Tests")
    class PrePersistTests {

        @Test
        @DisplayName("@PrePersist should generate an id when null")
        void prePersist_generatesIdWhenNull() {
            Account acc = newAccount(new BigDecimal("0.00"), AccountStatus.ACTIVE);
            acc.setId(null);
            acc.prePersist();
            assertThat(acc.getId()).isNotNull().startsWith("ACC-");
        }

        @Test
        @DisplayName("@PrePersist should not override existing id")
        void prePersist_keepsExistingId() {
            Account acc = newAccount(new BigDecimal("0.00"), AccountStatus.ACTIVE);
            acc.setId("ACC-EXISTING");
            acc.prePersist();
            assertThat(acc.getId()).isEqualTo("ACC-EXISTING");
        }
    }

    @Nested
    @DisplayName("ToString Test")
    class ToStringTest {

        @Test
        @DisplayName("toString should include key fields")
        void toString_includesFields() {
            Account account = Account.builder()
                    .id("ACC-123")
                    .username("testuser")
                    .password("password123")
                    .holderName("Test User")
                    .balance(new BigDecimal("1000.00"))
                    .status(AccountStatus.ACTIVE)
                    .version(1)
                    .build();

            String result = account.toString();
            assertThat(result).contains("ACC-123");
            assertThat(result).contains("testuser");
            assertThat(result).contains("ACTIVE");
        }
    }
}
