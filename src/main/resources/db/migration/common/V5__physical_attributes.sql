CREATE TABLE physical_attributes
(
    prisoner_number  VARCHAR(7) NOT NULL,
    height_cm        INT,
    weight_kg        INT,
    hair             VARCHAR(81),
    facial_hair      VARCHAR(81),
    face             VARCHAR(81),
    build            VARCHAR(81),
    left_eye_colour  VARCHAR(81),
    right_eye_colour VARCHAR(81),

    CONSTRAINT physical_attributes_pk PRIMARY KEY (prisoner_number),
    CONSTRAINT hair_reference_data_code_fk FOREIGN KEY (hair) REFERENCES reference_data_code (id),
    CONSTRAINT facial_hair_reference_data_code_fk FOREIGN KEY (facial_hair) REFERENCES reference_data_code (id),
    CONSTRAINT face_reference_data_code_fk FOREIGN KEY (face) REFERENCES reference_data_code (id),
    CONSTRAINT build_reference_data_code_fk FOREIGN KEY (build) REFERENCES reference_data_code (id),
    CONSTRAINT left_eye_colour_reference_data_code_fk FOREIGN KEY (left_eye_colour) REFERENCES reference_data_code (id),
    CONSTRAINT right_eye_colour_reference_data_code_fk FOREIGN KEY (right_eye_colour) REFERENCES reference_data_code (id)
);

COMMENT ON TABLE physical_attributes IS 'The physical attributes of a prisoner';
COMMENT ON COLUMN physical_attributes.prisoner_number IS 'Primary key - the identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN physical_attributes.height_cm IS 'Prisoner height, in centimetres';
COMMENT ON COLUMN physical_attributes.weight_kg IS 'Prisoner weight, in kilograms';
COMMENT ON COLUMN physical_attributes.hair IS 'Prisoner hair type or colour, from reference data domain HAIR';
COMMENT ON COLUMN physical_attributes.facial_hair IS 'Prisoner facial hair type, from reference data domain FACIAL_HAIR';
COMMENT ON COLUMN physical_attributes.face IS 'Prisoner face shape, from reference data domain FACE';
COMMENT ON COLUMN physical_attributes.build IS 'Prisoner build, from reference data domain BUILD';
COMMENT ON COLUMN physical_attributes.left_eye_colour IS 'Prisoner left eye colour, from reference data domain EYE';
COMMENT ON COLUMN physical_attributes.right_eye_colour IS 'Prisoner right eye colour, from reference data domain EYE';

GRANT SELECT, INSERT, UPDATE, DELETE ON physical_attributes TO prison_person;
