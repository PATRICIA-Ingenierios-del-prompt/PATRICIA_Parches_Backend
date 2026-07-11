-- V4 — reportes de miembros de un parche. Append-only (sin version, sin
-- updates esperados). Se relacionan con parches por parche_id (NO viven
-- dentro de ParcheEntity), asi cada reporte es una fila independiente y
-- filtrar por parche es un index scan.

CREATE TABLE parche_reports (
    report_id    UUID         NOT NULL PRIMARY KEY,
    parche_id    UUID         NOT NULL,
    creator_id   UUID         NOT NULL,
    reported_id  UUID         NOT NULL,
    report_type  VARCHAR(32)  NOT NULL,
    description  VARCHAR(1000),
    created_at   TIMESTAMP    NOT NULL
);

-- Query principal: listar reportes de un parche (findByParcheId).
CREATE INDEX idx_parche_reports_parche_id ON parche_reports (parche_id);

-- Utilidad para auditoria futura (quien fue reportado / quien reporta).
CREATE INDEX idx_parche_reports_reported_id ON parche_reports (reported_id);
CREATE INDEX idx_parche_reports_creator_id  ON parche_reports (creator_id);
