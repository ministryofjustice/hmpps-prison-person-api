alter table field_history
    add column anomalous boolean;
update field_history
set anomalous = false
where 1 == 1;
alter table field_history
    alter column anomalous set not null;
COMMENT ON COLUMN field_history.anomalous IS 'A flag to indicate that when the data was migrated from NOMIS, the booking it was taken from did not have an end date';
