package com.banking.transfer.repository;

import com.banking.transfer.entity.TransactionLog;
import com.banking.transfer.entity.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionLogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @Test
    @DisplayName("findByIdempotencyKey returns transaction when it exists")
    void findByIdempotencyKey_returnsTransaction() {
        // given
        TransactionLog tx = new TransactionLog();
        tx.setId("tx-1"); // explicitly setting id is fine
        tx.setFromAccountId("acc-1");
        tx.setToAccountId("acc-2");
        tx.setAmount(BigDecimal.TEN);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setIdempotencyKey("idem-key-1");
        tx.setCreatedOn(LocalDateTime.now());

        entityManager.persist(tx);
        entityManager.flush();

        // when
        Optional<TransactionLog> found = transactionLogRepository.findByIdempotencyKey("idem-key-1");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("tx-1");
        assertThat(found.get().getFromAccountId()).isEqualTo("acc-1");
        assertThat(found.get().getToAccountId()).isEqualTo("acc-2");
        assertThat(found.get().getAmount()).isEqualByComparingTo("10");
        assertThat(found.get().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("findByAccountId returns transactions where account is sender or receiver, ordered by createdOn DESC")
    void findByAccountId_returnsRelatedTransactions() {
        // given
        LocalDateTime now = LocalDateTime.now();

        TransactionLog tx1 = new TransactionLog();
        tx1.setId("tx-1");
        tx1.setFromAccountId("acc-1"); // sender
        tx1.setToAccountId("acc-2");
        tx1.setAmount(BigDecimal.TEN);
        tx1.setStatus(TransactionStatus.SUCCESS);
        tx1.setIdempotencyKey("key-1");
        tx1.setCreatedOn(now.minusHours(1)); // older
        entityManager.persist(tx1);

        TransactionLog tx2 = new TransactionLog();
        tx2.setId("tx-2");
        tx2.setFromAccountId("acc-3");
        tx2.setToAccountId("acc-1"); // receiver
        tx2.setAmount(BigDecimal.valueOf(20));
        tx2.setStatus(TransactionStatus.SUCCESS);
        tx2.setIdempotencyKey("key-2");
        tx2.setCreatedOn(now); // newer
        entityManager.persist(tx2);

        TransactionLog tx3 = new TransactionLog();
        tx3.setId("tx-3");
        tx3.setFromAccountId("acc-4");
        tx3.setToAccountId("acc-5"); // unrelated
        tx3.setAmount(BigDecimal.valueOf(30));
        tx3.setStatus(TransactionStatus.SUCCESS);
        tx3.setIdempotencyKey("key-3");
        tx3.setCreatedOn(now);
        entityManager.persist(tx3);

        entityManager.flush();

        // when
        List<TransactionLog> results = transactionLogRepository.findByAccountId("acc-1");

        // then
        assertThat(results).hasSize(2);

        // Because the JPQL orders by createdOn DESC, tx2 (newer) should come before tx1
        // (older)
        assertThat(results.get(0).getId()).isEqualTo("tx-2");
        assertThat(results.get(1).getId()).isEqualTo("tx-1");
    }
}