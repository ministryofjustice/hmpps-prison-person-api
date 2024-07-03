CREATE TABLE field_metadata
(
    prisoner_number             VARCHAR(7)                  NOT NULL,
    field                       VARCHAR(40),
    last_modified_at            TIMESTAMP WITH TIME ZONE    NOT NULL,
    last_modified_by            VARCHAR(40)                 NOT NULL,

    CONSTRAINT field_update_pk PRIMARY KEY (prisoner_number, field)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON field_metadata TO prison_person;
