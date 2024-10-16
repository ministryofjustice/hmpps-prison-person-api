ALTER TABLE field_history
    ADD COLUMN anomalous boolean NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN field_history.anomalous IS 'A flag to indicate that when the data was migrated from NOMIS, the booking it was taken from was historical but did not have an end date';
