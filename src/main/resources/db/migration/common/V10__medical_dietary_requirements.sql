CREATE TABLE medical_dietary_requirements
(
    id                  BIGSERIAL   NOT NULL,
    prisoner_number     VARCHAR(7)  NOT NULL,
    dietary_requirement VARCHAR(81) NOT NULL,
    other_text          VARCHAR(255),

    CONSTRAINT medical_dietary_requirements_fk PRIMARY KEY (id),
    CONSTRAINT dietary_requirement_fk FOREIGN KEY (dietary_requirement) REFERENCES reference_data_code (id)
);

CREATE INDEX medical_dietary_requirements_prisoner_number_idx ON medical_dietary_requirements (prisoner_number);

COMMENT ON TABLE medical_dietary_requirements IS 'The list of food allergies the prisoner has';
COMMENT ON COLUMN medical_dietary_requirements.id IS 'The primary key, in case prisoners require multiple "other" values';
COMMENT ON COLUMN medical_dietary_requirements.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN medical_dietary_requirements.dietary_requirement IS 'The dietary requirement relevant to a prisoner (the prisoner can have more than one)';
COMMENT ON COLUMN medical_dietary_requirements.other_text IS 'The text used for when someone enters an allergy of "other"';

GRANT SELECT, INSERT, UPDATE, DELETE ON medical_dietary_requirements TO prison_person;
