-- Reference Data Domain

CREATE TABLE reference_data_domain
(
    code             VARCHAR(40)              NOT NULL,
    description      VARCHAR(100)             NOT NULL,
    list_sequence    INT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(40)              NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE,
    last_modified_by VARCHAR(40),
    deactivated_at   TIMESTAMP WITH TIME ZONE,
    deactivated_by   VARCHAR(40),
    migrated_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT reference_data_domain_pk PRIMARY KEY (code)
);

CREATE INDEX reference_data_domain_description_idx ON reference_data_domain (description);
CREATE INDEX reference_data_domain_list_sequence_idx ON reference_data_domain (list_sequence);
CREATE INDEX reference_data_domain_created_at_idx ON reference_data_domain (created_at);
CREATE INDEX reference_data_domain_deactivated_at_idx ON reference_data_domain (deactivated_at);

COMMENT ON TABLE reference_data_domain IS 'Reference data domains for prison person data';

GRANT SELECT, INSERT, UPDATE, DELETE ON reference_data_domain TO prison_person;

-- Reference Data Code

CREATE TABLE reference_data_code
(
    domain           VARCHAR(40)              NOT NULL REFERENCES reference_data_domain (code),
    code             VARCHAR(40)              NOT NULL,
    description      VARCHAR(100)             NOT NULL,
    list_sequence    INT DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by       VARCHAR(40)              NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE,
    last_modified_by VARCHAR(40),
    deactivated_at   TIMESTAMP WITH TIME ZONE,
    deactivated_by   VARCHAR(40),
    migrated_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT reference_data_code_pk PRIMARY KEY (code, domain)
);

CREATE INDEX reference_data_code_description_idx ON reference_data_code (description);
CREATE INDEX reference_data_code_list_sequence_idx ON reference_data_code (list_sequence);
CREATE INDEX reference_data_code_created_at_idx ON reference_data_code (created_at);
CREATE INDEX reference_data_code_deactivated_at_idx ON reference_data_code (deactivated_at);

COMMENT ON TABLE reference_data_code IS 'Reference data codes for prison person data';

GRANT SELECT, INSERT, UPDATE, DELETE ON reference_data_code TO prison_person;
