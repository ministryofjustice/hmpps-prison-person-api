ALTER TABLE identifying_mark_image
    ADD COLUMN latest bool NOT NULL DEFAULT true;

COMMENT ON COLUMN identifying_mark_image.latest IS 'Whether or not the image is the latest uploaded for the mark';

