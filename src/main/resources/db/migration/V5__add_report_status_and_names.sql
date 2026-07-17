ALTER TABLE parche_reports
    ADD COLUMN status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN reported_user_name  VARCHAR(255),
    ADD COLUMN parche_name         VARCHAR(255),
    ADD COLUMN resolved_at         TIMESTAMP;

CREATE INDEX idx_parche_reports_status ON parche_reports (status);
