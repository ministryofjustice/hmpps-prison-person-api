CREATE TABLE food_allergies
(
    prisoner_number VARCHAR(7) NOT NULL,
    allergy         VARCHAR(81) NOT NULL,
    other_text      VARCHAR(255),

    CONSTRAINT food_allergy_fk PRIMARY KEY (prisoner_number, allergy),
    CONSTRAINT allergy_fk FOREIGN KEY (allergy) REFERENCES reference_data_code (id)
);

COMMENT ON TABLE food_allergies IS 'The list of food allergies the prisoner has';
COMMENT ON COLUMN food_allergies.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...). Forms part of the primary key along with the allergy';
COMMENT ON COLUMN food_allergies.allergy IS 'The allergy relevant to a prisoner (the prisoner can have more than one). Forms the other part of the primary key along with the prisoner number';
COMMENT ON COLUMN food_allergies.other_text IS 'The text used for when someone enters an allergy of "other"';

GRANT SELECT, INSERT, UPDATE, DELETE ON food_allergies TO prison_person;
