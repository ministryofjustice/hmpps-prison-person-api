ALTER TABLE identifying_mark_image
    ADD COLUMN latest bool NOT NULL DEFAULT true;

COMMENT ON COLUMN identifying_mark_image.latest IS 'Whether or not the image is the latest uploaded for the mark';

-- Ensures that only one image can be marked as 'latest' for a given mark:
CREATE UNIQUE INDEX identifying_mark_image_latest_idx ON identifying_mark_image (identifying_mark_id) WHERE latest = true;
