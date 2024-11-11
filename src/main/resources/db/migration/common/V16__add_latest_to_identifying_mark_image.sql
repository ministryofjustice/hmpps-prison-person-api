ALTER TABLE identifying_mark_image
    ADD COLUMN latest bool;

-- All existing images are by default the latest
UPDATE identifying_mark_image SET latest = true;

COMMENT ON COLUMN identifying_mark_image.latest IS 'Whether or not the image is the latest uploaded for the mark';

