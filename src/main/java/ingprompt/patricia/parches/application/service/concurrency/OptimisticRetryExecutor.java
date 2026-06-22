package ingprompt.patricia.parches.application.service.concurrency;

import ingprompt.patricia.parches.domain.exception.ParcheConcurrentModificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Slf4j
@Component
public class OptimisticRetryExecutor {
    private static final int MAX_ATTEMPTS = 4;
    private static final long BASE_BACKOFF_MS = 20L;

    private final PlatformTransactionManager txManager;

    public OptimisticRetryExecutor(PlatformTransactionManager txManager) {
        this.txManager = txManager;
    }

    public void runRetrying(UUID parcheIdForReporting, Runnable action) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                tx.executeWithoutResult(status -> action.run());
                return;
            } catch (OptimisticLockingFailureException conflict) {
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("Optimistic-lock retries exhausted for parche {} ({} attempts)",
                            parcheIdForReporting, attempt);
                    throw new ParcheConcurrentModificationException(parcheIdForReporting);
                }
                log.debug("Optimistic-lock conflict on parche {} (attempt {} of {}); retrying",
                        parcheIdForReporting, attempt, MAX_ATTEMPTS);
                backoff(attempt);
            }
        }
    }

    private static void backoff(int attempt) {
        // Linear-ish backoff + small random jitter; max ~80ms even on attempt 4.
        long sleep = BASE_BACKOFF_MS * attempt + (long) (Math.random() * BASE_BACKOFF_MS);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to retry optimistic-lock conflict", e);
        }
    }
}
