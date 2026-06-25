package ingprompt.patricia.parches.application.service.concurrency;

import ingprompt.patricia.parches.domain.exception.ParcheConcurrentModificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimisticRetryExecutorTest {

    @Mock
    private PlatformTransactionManager txManager;

    @Test
    void runRetrying_runsActionOnce_whenNoConflict() {
        when(txManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        OptimisticRetryExecutor executor = new OptimisticRetryExecutor(txManager);
        AtomicInteger calls = new AtomicInteger();

        executor.runRetrying(UUID.randomUUID(), calls::incrementAndGet);

        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void runRetrying_exhaustsRetries_thenThrowsDomainException() {
        when(txManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        OptimisticRetryExecutor executor = new OptimisticRetryExecutor(txManager);
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> executor.runRetrying(UUID.randomUUID(), () -> {
            calls.incrementAndGet();
            throw new OptimisticLockingFailureException("conflict");
        })).isInstanceOf(ParcheConcurrentModificationException.class);

        assertThat(calls.get()).isEqualTo(4); // MAX_ATTEMPTS
    }
}
