CREATE TABLE physical_attributes
(
    prisoner_number             VARCHAR(7)                  NOT NULL,
    height_cm                   INT,
    weight_kg                   INT,
    created_at                  TIMESTAMP WITH TIME ZONE    NOT NULL,
    created_by                  VARCHAR(40),
    last_modified_at            TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_by            VARCHAR(40),
    migrated_at                 TIMESTAMP WITH TIME ZONE,

    CONSTRAINT physical_attributes_pk PRIMARY KEY (prisoner_number)
);

COMMENT ON TABLE physical_attributes IS 'The height and weight of a prisoner';
COMMENT ON COLUMN physical_attributes.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN physical_attributes.height_cm IS 'Prisoner height, in centimetres';
COMMENT ON COLUMN physical_attributes.weight_kg IS 'Prisoner weight, in kilograms';
COMMENT ON COLUMN physical_attributes.created_at IS 'Timestamp of creation';
COMMENT ON COLUMN physical_attributes.created_by IS 'If available, the username of the user creating the record';
COMMENT ON COLUMN physical_attributes.last_modified_at IS 'Timestamp of last modification';
COMMENT ON COLUMN physical_attributes.last_modified_by IS 'If available, the username of the user modifying the record';
COMMENT ON COLUMN physical_attributes.migrated_at IS 'If the record has been migrated from NOMIS, the timestamp of migration';

--

CREATE TABLE physical_attributes_history
(
    physical_attributes_history_id  BIGSERIAL                   NOT NULL,
    prisoner_number                 VARCHAR(7)                  NOT NULL,
    height_cm                       INT,
    weight_kg                       INT,
    applies_from                    TIMESTAMP WITH TIME ZONE    NOT NULL,
    applies_to                      TIMESTAMP WITH TIME ZONE,
    created_at                      TIMESTAMP WITH TIME ZONE    NOT NULL,
    created_by                      VARCHAR(40),
    migrated_at                     TIMESTAMP WITH TIME ZONE,

    CONSTRAINT physical_attributes_history_pk PRIMARY KEY (physical_attributes_history_id),
    CONSTRAINT physical_attributes_history_prisoner_number_fk FOREIGN KEY (prisoner_number) REFERENCES physical_attributes(prisoner_number)
);

COMMENT ON TABLE physical_attributes_history IS 'The history of the height and weight of a prisoner';
COMMENT ON COLUMN physical_attributes_history.applies_from IS 'The timestamp from which these physical attributes were true of the prisoner. This is potentially different to the created_at timestamp to accommodate for the possibility that this was retroactively recorded data.';
COMMENT ON COLUMN physical_attributes_history.applies_to IS 'The timestamp at which these physical attributes were no longer true of the prisoner. This should be populated for an old history record whenever a new history record is created.';

--

CREATE TABLE physical_attributes_migration
(
    physical_attributes_migration_id    BIGSERIAL                   NOT NULL,
    physical_attributes_history_id      BIGSERIAL                   NOT NULL,
    offender_book_id                    BIGINT                      NOT NULL,
    attribute_seq                       INT                         NOT NULL,
    migrated_at                         TIMESTAMP WITH TIME ZONE    NOT NULL,

    CONSTRAINT physical_attributes_migration_pk PRIMARY KEY (physical_attributes_migration_id),
    CONSTRAINT physical_attributes_migration_history_id_fk FOREIGN KEY (physical_attributes_history_id) REFERENCES physical_attributes_history(physical_attributes_history_id)
);

COMMENT ON TABLE physical_attributes_migration IS 'Provides details of how a migrated physical attribute links to data in NOMIS';
COMMENT ON COLUMN physical_attributes_migration.offender_book_id IS 'The NOMIS booking number that this data was associated with';
COMMENT ON COLUMN physical_attributes_migration.attribute_seq IS 'Versioning used in NOMIS to provide a history of edits to the physical attributes for a given booking';
