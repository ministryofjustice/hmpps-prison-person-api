CREATE TABLE physical_attributes
(
    prisoner_number VARCHAR(7) NOT NULL,
    height_cm       INT,
    weight_kg       INT,
    hair            VARCHAR(81),
    facial_hair     VARCHAR(81),
    face            VARCHAR(81),
    build           VARCHAR(81),

    CONSTRAINT physical_attributes_pk PRIMARY KEY (prisoner_number),
    CONSTRAINT hair_reference_data_code_fk FOREIGN KEY (hair) REFERENCES reference_data_code (id)
);

COMMENT ON TABLE physical_attributes IS 'The physical attributes of a prisoner';
COMMENT ON COLUMN physical_attributes.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN physical_attributes.height_cm IS 'Prisoner height, in centimetres';
COMMENT ON COLUMN physical_attributes.weight_kg IS 'Prisoner weight, in kilograms';
COMMENT ON COLUMN physical_attributes.hair IS 'Prisoner hair type or colour, from reference data domain HAIR';
COMMENT ON COLUMN physical_attributes.facial_hair IS 'Prisoner facial hair type, from reference data domain FACIAL_HAIR';
COMMENT ON COLUMN physical_attributes.face IS 'Prisoner face shape, from reference data domain FACE';
COMMENT ON COLUMN physical_attributes.build IS 'Prisoner build, from reference data domain BUILD';

GRANT SELECT, INSERT, UPDATE, DELETE ON physical_attributes TO prison_person;
