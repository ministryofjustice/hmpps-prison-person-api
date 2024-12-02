CREATE TABLE distinguishing_mark_history
(
    history_id             BIGSERIAL                NOT NULL,
    distinguishing_mark_id UUID                     NOT NULL,
    value_json             text,
    applies_from           TIMESTAMP WITH TIME ZONE NOT NULL,
    applies_to             TIMESTAMP WITH TIME ZONE,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by             VARCHAR(40)              NOT NULL,
    migrated_at            TIMESTAMP WITH TIME ZONE,
    merged_at              TIMESTAMP WITH TIME ZONE,
    merged_from            VARCHAR(7),
    source                 VARCHAR(10),
    anomalous              BOOLEAN                  NOT NULL DEFAULT FALSE,

    CONSTRAINT distinguishing_mark_history_pk PRIMARY KEY (history_id),
    CONSTRAINT distinguishing_mark_history_fk FOREIGN KEY (distinguishing_mark_id) REFERENCES prison_person.distinguishing_mark (distinguishing_mark_id)
);

CREATE INDEX distinguishing_mark_idx ON distinguishing_mark_history (distinguishing_mark_id);

COMMENT ON TABLE distinguishing_mark_history IS 'The history for a distinguishing mark';
COMMENT ON COLUMN distinguishing_mark_history.distinguishing_mark_id IS 'The ID of the distinguishing mark it is related to';
COMMENT ON COLUMN distinguishing_mark_history.value_json IS 'The JSON representation of the distinguishing mark at the point in the history it is recorded';
COMMENT ON COLUMN distinguishing_mark_history.applies_from IS 'The timestamp from which the field value was true of the prisoner. This is potentially different to the created_at timestamp to accommodate for the possibility that this was retroactively recorded data.';
COMMENT ON COLUMN distinguishing_mark_history.applies_to IS 'The timestamp at which the field was no longer true of the prisoner. This should be populated for an old history record whenever a new history record is created.';
COMMENT ON COLUMN distinguishing_mark_history.created_at IS 'Timestamp of when the history record was created';
COMMENT ON COLUMN distinguishing_mark_history.created_by IS 'The username of the user creating the history record';
COMMENT ON COLUMN distinguishing_mark_history.migrated_at IS 'Timestamp of when the history record was migrated from NOMIS';
COMMENT ON COLUMN distinguishing_mark_history.merged_at IS 'Timestamp of when the history record was merged from another prisoner number';
COMMENT ON COLUMN distinguishing_mark_history.merged_from IS 'The old prisoner number that this history item was merged from';
COMMENT ON COLUMN distinguishing_mark_history.source IS 'Either DPS or NOMIS. Will be NOMIS if the record was either migrated or synced from NOMIS';
COMMENT ON COLUMN distinguishing_mark_history.anomalous IS 'A flag to indicate that when the data was migrated from NOMIS, the booking it was taken from was historical but did not have an end date';

GRANT SELECT, INSERT, UPDATE, DELETE ON distinguishing_mark_history TO prison_person;
