CREATE TABLE identifying_marks
(
    identifying_mark_id      BIGSERIAL                NOT NULL,
    prisoner_number          VARCHAR(7)               NOT NULL,
    body_part_code           VARCHAR(12),
    mark_type                VARCHAR(12),
    side_code                VARCHAR(12),
    part_orientation         VARCHAR(12),
    comment_Text             VARCHAR(240),
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by               VARCHAR(40)              NOT NULL,

    CONSTRAINT identifying_mark_pk PRIMARY KEY (identifying_mark_id),
    CONSTRAINT body_part_code_reference_data_code_fk FOREIGN KEY (body_part_code) REFERENCES reference_data_code (id),
    CONSTRAINT mark_type_reference_data_code_fk FOREIGN KEY (mark_type) REFERENCES reference_data_code (id),
    CONSTRAINT side_code_reference_data_code_fk FOREIGN KEY (side_code) REFERENCES reference_data_code (id),
    CONSTRAINT part_orientation_reference_data_code_fk FOREIGN KEY (part_orientation) REFERENCES reference_data_code (id)
);

COMMENT ON COLUMN identifying_marks.prisoner_number IS 'The identifier of a prisoner (also often called prison number, NOMS number, offender number...)';
COMMENT ON COLUMN identifying_marks.body_part_code IS 'The reference_data_code.id for the field if the field represents a foreign key to reference_data_code';
COMMENT ON COLUMN identifying_marks.mark_type IS 'The reference_data_code.id for the field if the field represents a foreign key to reference_data_code';
COMMENT ON COLUMN identifying_marks.side_code IS 'The reference_data_code.id for the field if the field represents a foreign key to reference_data_code';
COMMENT ON COLUMN identifying_marks.part_orientation IS 'The reference_data_code.id for the field if the field represents a foreign key to reference_data_code';
COMMENT ON COLUMN identifying_marks.comment_text IS 'The comment describing the identifying mark';
COMMENT ON COLUMN identifying_marks.created_at IS 'Timestamp of when the identifying_mark record was created';
COMMENT ON COLUMN identifying_marks.created_by IS 'The username of the user creating the identifying_mark record';

GRANT SELECT, INSERT, UPDATE, DELETE ON identifying_marks TO prison_person;


CREATE TABLE identifying_marks_images
(
    identifying_mark_image_id BIGSERIAL                NOT NULL,
    identifying_mark_id       BIGINT                   NOT NULL,

    CONSTRAINT identifying_mark_image_pk PRIMARY KEY (identifying_mark_image_id),
    CONSTRAINT identifying_mark_image_identifying_mark_fk FOREIGN KEY (identifying_mark_id) REFERENCES identifying_marks (identifying_mark_id)
);

COMMENT ON COLUMN identifying_marks_images.identifying_mark_image_id IS 'The uuid of an image document stored in the document service';
COMMENT ON COLUMN identifying_marks_images.identifying_mark_id IS 'The identifying_marks.identifying_mark_id for the field if the field represents a foreign key to identifying_marks';

GRANT SELECT, INSERT, UPDATE, DELETE ON identifying_marks_images TO prison_person;