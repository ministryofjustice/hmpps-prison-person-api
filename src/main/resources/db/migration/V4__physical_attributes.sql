CREATE TABLE physical_attributes
(
    prisoner_number             VARCHAR(7)                  NOT NULL,
    height_cm                   INT,
    weight_kg                   INT,

    CONSTRAINT physical_attributes_pk PRIMARY KEY (prisoner_number)
);

COMMENT ON TABLE physical_attributes IS 'The physical attributes of a prisoner';
COMMENT ON COLUMN physical_attributes.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN physical_attributes.height_cm IS 'Prisoner height, in centimetres';
COMMENT ON COLUMN physical_attributes.weight_kg IS 'Prisoner weight, in kilograms';

GRANT SELECT, INSERT, UPDATE, DELETE ON physical_attributes TO prison_person;
