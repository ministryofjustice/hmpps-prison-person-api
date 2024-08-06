-- Seed data for tests
INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('TEST', 'Test domain', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('HAIR', 'Hair type or colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by, last_modified_at,
                                   last_modified_by, deactivated_at, deactivated_by)
VALUES ('INACTIVE', 'Inactive domain for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100',
        'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('TEST_ORANGE', 'TEST', 'ORANGE', 'Orange', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('TEST_BROWN', 'TEST', 'BROWN', 'Brown', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('TEST_RED', 'TEST', 'RED', 'Red', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('TEST_WHITE', 'TEST', 'WHITE', 'White', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('HAIR_BLACK', 'HAIR', 'BLACK', 'Black', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('HAIR_GREY', 'HAIR', 'GREY', 'Grey', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('HAIR_BLONDE', 'HAIR', 'BLONDE', 'Blonde', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by, last_modified_at,
                                 last_modified_by, deactivated_at, deactivated_by)
VALUES ('TEST_INACTIVE', 'TEST', 'INACTIVE', 'Inactive code for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER',
        '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');
