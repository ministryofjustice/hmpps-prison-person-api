ALTER TABLE identifying_mark RENAME TO distinguishing_mark;

ALTER TABLE distinguishing_mark
    RENAME COLUMN identifying_mark_id TO distinguishing_mark_id;

ALTER TABLE distinguishing_mark
    RENAME CONSTRAINT identifying_mark_pk TO distinguishing_mark_pk;

ALTER TABLE identifying_mark_image RENAME TO distinguishing_mark_image;

ALTER TABLE distinguishing_mark_image
    RENAME CONSTRAINT identifying_mark_image_pk TO distinguishing_mark_image_pk;

ALTER TABLE distinguishing_mark_image
    RENAME CONSTRAINT identifying_mark_image_identifying_mark_fk TO distinguishing_mark_image_distinguishing_mark_fk;

ALTER TABLE distinguishing_mark_image
    RENAME COLUMN identifying_mark_id TO distinguishing_mark_id;

ALTER TABLE distinguishing_mark_image
    RENAME COLUMN identifying_mark_image_id TO distinguishing_mark_image_id;
