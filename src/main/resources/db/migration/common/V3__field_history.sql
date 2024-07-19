CREATE TABLE field_history
(
    field_history_id            BIGSERIAL                   NOT NULL,
    prisoner_number             VARCHAR(7)                  NOT NULL,
    field                       VARCHAR(40),
    value_int                   INT,
    applies_from                TIMESTAMP WITH TIME ZONE    NOT NULL,
    applies_to                  TIMESTAMP WITH TIME ZONE,
    created_at                  TIMESTAMP WITH TIME ZONE    NOT NULL,
    created_by                  VARCHAR(40)                 NOT NULL,
    migrated_at                 TIMESTAMP WITH TIME ZONE,
    merged_at                   TIMESTAMP WITH TIME ZONE,
    merged_from                 VARCHAR(7),
    source                      VARCHAR(10),

    CONSTRAINT field_history_pk PRIMARY KEY (field_history_id)
);

CREATE INDEX field_history_prisoner_number_field_idx ON field_history(prisoner_number, field);

COMMENT ON TABLE field_history IS 'The field level history of prisoner physical attributes';
COMMENT ON COLUMN field_history.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN field_history.field IS 'The field that this history record is for';
COMMENT ON COLUMN field_history.value_int IS 'The integer value for the field if the field represents an integer';
COMMENT ON COLUMN field_history.applies_from IS 'The timestamp from which the field value was true of the prisoner. This is potentially different to the created_at timestamp to accommodate for the possibility that this was retroactively recorded data.';
COMMENT ON COLUMN field_history.applies_to IS 'The timestamp at which the field was no longer true of the prisoner. This should be populated for an old history record whenever a new history record is created.';
COMMENT ON COLUMN field_history.created_at IS 'The timestamp from which the field value was true of the prisoner. This is potentially different to the created_at timestamp to accommodate for the possibility that this was retroactively recorded data.';
COMMENT ON COLUMN field_history.applies_to IS 'The timestamp at which the field was no longer true of the prisoner. This should be populated for an old history record whenever a new history record is created.';


GRANT SELECT, INSERT, UPDATE, DELETE ON field_history TO prison_person;
