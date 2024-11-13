ALTER TABLE identifying_mark RENAME TO distinguishing_mark;

alter table distinguishing_mark
    rename column identifying_mark_id to distinguishing_mark_id;

alter table distinguishing_mark
    rename constraint identifying_mark_pk to distinguishing_mark_pk;

ALTER TABLE identifying_mark_image RENAME TO distinguishing_mark_image;

alter table distinguishing_mark_image
    rename constraint identifying_mark_image_pk to distinguishing_mark_image_pk;

alter table distinguishing_mark_image
    rename constraint identifying_mark_image_identifying_mark_fk to distinguishing_mark_image_distinguishing_mark_fk;

alter table distinguishing_mark_image
    rename column identifying_mark_id to distinguishing_mark_id;

alter table distinguishing_mark_image
    rename column identifying_mark_image_id to distinguishing_mark_image_id;
