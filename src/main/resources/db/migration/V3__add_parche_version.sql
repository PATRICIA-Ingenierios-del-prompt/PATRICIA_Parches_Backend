-- Optimistic-lock token. Hibernate increments on every UPDATE; mismatch on save
-- triggers ObjectOptimisticLockingFailureException which the service retries.
ALTER TABLE parches
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
