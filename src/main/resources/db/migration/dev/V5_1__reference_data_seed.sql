-- Seed data for dev
INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('HAIR', 'Hair type or colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACIAL_HAIR', 'Facial hair type', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACE', 'Face shape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('BUILD', 'Build', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('EYE', 'Eye colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (domain, code, description, list_sequence, created_at, created_by)
VALUES
-- Hair
('HAIR', 'BALD', 'Bald', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'BALDING', 'Balding', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'BLACK', 'Black', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'BLONDE', 'Blonde', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'BROWN', 'Brown', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'BRUNETTE', 'Brunette', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'DARK', 'Dark', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'DYED', 'Dyed', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'GINGER', 'Ginger', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'GREY', 'Grey', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'LIGHT', 'Light', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'MOUSE', 'Mouse', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'RED', 'Red', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('HAIR', 'WHITE', 'White', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Facial hair
('FACIAL_HAIR', 'BEARDED', 'Full Beard', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR', 'CLEAN SHAVEN', 'Clean Shaven', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR', 'GOATEE', 'Goatee Beard', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR', 'MOUSTACHE', 'Moustache Only', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR', 'NA', 'Not Asked', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACIAL_HAIR', 'SIDEBURNS', 'Sideburns', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Face shape
('FACE', 'ANGULAR', 'Angular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE', 'BULLET', 'Bullet', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE', 'OVAL', 'Oval', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE', 'ROUND', 'Round', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE', 'SQUARE', 'Square', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('FACE', 'TRIANGULAR', 'Triangular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Build
('BUILD', 'FRAIL', 'Frail', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'HEAVY', 'Heavy', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'MEDIUM', 'Medium', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'MUSC', 'Muscular', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'OBESE', 'Obese', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'PROP', 'Proportional', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'SLIGHT', 'Slight', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'SMALL', 'Small', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'STOCKY', 'Stocky', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'STOOPED', 'Stooped', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('BUILD', 'THIN', 'Thin', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Eye colour
('EYE', 'BLUE', 'Blue', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'BROWN', 'Brown', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'CLOUDED', 'Clouded', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'GREEN', 'Green', 1, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'GREY', 'Grey', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'HAZEL', 'Hazel', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'MISSING', 'Missing', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
('EYE', 'PINK', 'Pink', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');