package com.banking.transfer.repository;

import com.banking.transfer.entity.Account;
import com.banking.transfer.entity.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("findByUsername returns account when it exists")
    void findByUsername_returnsAccount() {
        // given
        Account account = Account.builder()
                .id(null) // let @PrePersist in Account set the ID if needed
                .username("testuser")
                .password("pass")
                .holderName("Test User")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(account);
        entityManager.flush();

        // when
        Optional<Account> found = accountRepository.findByUsername("testuser");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("findByUsername returns empty when account does not exist")
    void findByUsername_returnsEmpty() {
        Optional<Account> found = accountRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername returns true when account exists")
    void existsByUsername_returnsTrue() {
        // given
        Account account = Account.builder()
                .id(null)
                .username("exists")
                .password("pass")
                .holderName("Exists User")
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        entityManager.persist(account);
        entityManager.flush();

        // when
        boolean exists = accountRepository.existsByUsername("exists");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername returns false when account does not exist")
    void existsByUsername_returnsFalse() {
        boolean exists = accountRepository.existsByUsername("nobody");
        assertThat(exists).isFalse();
    }
}