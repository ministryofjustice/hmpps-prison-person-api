ALTER TABLE field_history
    ADD COLUMN anomalous boolean;
UPDATE field_history
SET anomalous = FALSE
WHERE 1 = 1;
ALTER TABLE field_history
    ALTER COLUMN anomalous SET NOT NULL;
COMMENT ON COLUMN field_history.anomalous IS 'A flag to indicate that when the data was migrated from NOMIS, the booking it was taken from was historical but did not have an end date';
