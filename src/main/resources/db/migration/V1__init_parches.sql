-- Parches table: lifecycle, ownership, visibility, capacity, status and embedded
-- value objects (CommunicationChannels, CollaborationTools).
CREATE TABLE parches (
    parche_id          UUID         PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    description        VARCHAR(1000),
    owner_id           UUID         NOT NULL,
    category           VARCHAR(50)  NOT NULL,
    visibility         VARCHAR(50)  NOT NULL,
    status             VARCHAR(50)  NOT NULL,
    max_capacity       INTEGER      NOT NULL,
    -- Embedded CommunicationChannels (default Hibernate column names)
    chat_id            UUID,
    voice_id           UUID,
    -- Embedded CollaborationTools (explicit @AttributeOverrides)
    collabs_parques_id UUID,
    collabs_canvas_id  UUID
);

CREATE INDEX idx_parches_owner_id   ON parches (owner_id);
CREATE INDEX idx_parches_visibility ON parches (visibility);
CREATE INDEX idx_parches_status     ON parches (status);

-- @ElementCollection mappings
CREATE TABLE parche_members (
    parche_id UUID NOT NULL REFERENCES parches (parche_id) ON DELETE CASCADE,
    member_id UUID NOT NULL,
    PRIMARY KEY (parche_id, member_id)
);

CREATE INDEX idx_parche_members_member_id ON parche_members (member_id);

CREATE TABLE parche_events (
    parche_id UUID NOT NULL REFERENCES parches (parche_id) ON DELETE CASCADE,
    event_id  UUID NOT NULL,
    PRIMARY KEY (parche_id, event_id)
);
