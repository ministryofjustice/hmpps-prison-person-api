-- Seed data for tests
INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('HAIR', 'Hair type or colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACIAL_HAIR', 'Facial hair type', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACE', 'Face shape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('BUILD', 'Build', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('EYE', 'Eye colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('SMOKE', 'Smoker or vaper', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES
-- Hair
('HAIR_BALD', 'HAIR', 'BALD', 'Bald', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_BALDING', 'HAIR', 'BALDING', 'Balding', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_BLACK', 'HAIR', 'BLACK', 'Black', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_BLONDE', 'HAIR', 'BLONDE', 'Blonde', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_BROWN', 'HAIR', 'BROWN', 'Brown', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_BRUNETTE', 'HAIR', 'BRUNETTE', 'Brunette', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_DARK', 'HAIR', 'DARK', 'Dark', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_DYED', 'HAIR', 'DYED', 'Dyed', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_GINGER', 'HAIR', 'GINGER', 'Ginger', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_GREY', 'HAIR', 'GREY', 'Grey', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_LIGHT', 'HAIR', 'LIGHT', 'Light', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_MOUSE', 'HAIR', 'MOUSE', 'Mouse', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_RED', 'HAIR', 'RED', 'Red', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR_WHITE', 'HAIR', 'WHITE', 'White', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Facial hair
('FACIAL_HAIR_BEARDED', 'FACIAL_HAIR', 'BEARDED', 'Full Beard', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR_CLEAN SHAVEN', 'FACIAL_HAIR', 'CLEAN SHAVEN', 'Clean Shaven', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR_GOATEE', 'FACIAL_HAIR', 'GOATEE', 'Goatee Beard', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR_MOUSTACHE', 'FACIAL_HAIR', 'MOUSTACHE', 'Moustache Only', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR_NA', 'FACIAL_HAIR', 'NA', 'Not Asked', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR_SIDEBURNS', 'FACIAL_HAIR', 'SIDEBURNS', 'Sideburns', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Face shape
('FACE_ANGULAR', 'FACE', 'ANGULAR', 'Angular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE_BULLET', 'FACE', 'BULLET', 'Bullet', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE_OVAL', 'FACE', 'OVAL', 'Oval', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE_ROUND', 'FACE', 'ROUND', 'Round', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE_SQUARE', 'FACE', 'SQUARE', 'Square', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE_TRIANGULAR', 'FACE', 'TRIANGULAR', 'Triangular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Build
('BUILD_FRAIL', 'BUILD', 'FRAIL', 'Frail', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_HEAVY', 'BUILD', 'HEAVY', 'Heavy', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_MEDIUM', 'BUILD', 'MEDIUM', 'Medium', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_MUSC', 'BUILD', 'MUSC', 'Muscular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_OBESE', 'BUILD', 'OBESE', 'Obese', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_PROP', 'BUILD', 'PROP', 'Proportional', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_SLIGHT', 'BUILD', 'SLIGHT', 'Slight', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_SMALL', 'BUILD', 'SMALL', 'Small', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_STOCKY', 'BUILD', 'STOCKY', 'Stocky', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_STOOPED', 'BUILD', 'STOOPED', 'Stooped', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD_THIN', 'BUILD', 'THIN', 'Thin', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Eye colour
('EYE_BLUE', 'EYE', 'BLUE', 'Blue', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_BROWN', 'EYE', 'BROWN', 'Brown', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_CLOUDED', 'EYE', 'CLOUDED', 'Clouded', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_GREEN', 'EYE', 'GREEN', 'Green', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_GREY', 'EYE', 'GREY', 'Grey', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_HAZEL', 'EYE', 'HAZEL', 'Hazel', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_MISSING', 'EYE', 'MISSING', 'Missing', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE_PINK', 'EYE', 'PINK', 'Pink', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Smoker or vaper
('SMOKE_SMOKER', 'SMOKE', 'SMOKER', 'Yes, they smoke', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('SMOKE_VAPER', 'SMOKE', 'VAPER', 'Yes, they smoke or use nicotine replacement therapy (NRT)', 0,
 '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('SMOKE_NO', 'SMOKE', 'NO', 'No, they do not smoke or vape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by, last_modified_at,
                                   last_modified_by, deactivated_at, deactivated_by)
VALUES ('INACTIVE', 'Inactive domain for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100',
        'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by, last_modified_at,
                                 last_modified_by, deactivated_at, deactivated_by)
VALUES ('FACE_INACTIVE', 'FACE', 'INACTIVE', 'Inactive code for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER',
        '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');
