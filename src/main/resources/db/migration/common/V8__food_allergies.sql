CREATE TABLE food_allergies
(
    id              BIGSERIAL   NOT NULL,
    prisoner_number VARCHAR(7)  NOT NULL,
    allergy         VARCHAR(81) NOT NULL,
    other_text      VARCHAR(255),

    CONSTRAINT food_allergy_fk PRIMARY KEY (id),
    CONSTRAINT allergy_fk FOREIGN KEY (allergy) REFERENCES reference_data_code (id)
);

COMMENT ON TABLE food_allergies IS 'The list of food allergies the prisoner has';
COMMENT ON COLUMN food_allergies.id IS 'The primary key, in case prisoners require multiple "other" values';
COMMENT ON COLUMN food_allergies.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN food_allergies.allergy IS 'The allergy relevant to a prisoner (the prisoner can have more than one)';
COMMENT ON COLUMN food_allergies.other_text IS 'The text used for when someone enters an allergy of "other"';

GRANT SELECT, INSERT, UPDATE, DELETE ON food_allergies TO prison_person;
