INSERT INTO identifying_mark (
   identifying_mark_id,
   prisoner_number,
   body_part_code,
   mark_type,
   side_code,
   part_orientation,
   comment_text,
   created_at,
   created_by
)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242257',
        '12345',
        'BODY_PART_FACE',
        'MARK_TYPE_SCAR',
        'SIDE_R',
        'PART_ORIENT_CENTR',
        'Large scar from fight',
        '2024-01-02 09:10:11.123',
        'USER_GEN'
    );

INSERT INTO identifying_mark (
    identifying_mark_id,
    prisoner_number,
    body_part_code,
    mark_type,
    side_code,
    part_orientation,
    comment_text,
    created_at,
    created_by
)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242258',
        '12345',
        'BODY_PART_FACE',
        'MARK_TYPE_SCAR',
        'SIDE_L',
        'PART_ORIENT_CENTR',
        'Another scar',
        '2024-01-02 09:10:11.123',
        'USER_GEN'
       );

INSERT INTO identifying_mark_image (
    identifying_mark_image_id,
    identifying_mark_id
)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242260', 'c46d0ce9-e586-4fa6-ae76-52ea8c242258');

INSERT INTO identifying_mark_image (
    identifying_mark_image_id,
    identifying_mark_id
)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242261', 'c46d0ce9-e586-4fa6-ae76-52ea8c242258');