ALTER TABLE field_history
    ADD COLUMN value_json text;

COMMENT ON COLUMN field_history.value_json IS 'Used for storing generic json data in text form';

