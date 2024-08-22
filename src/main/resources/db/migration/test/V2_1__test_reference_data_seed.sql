-- Seed data for tests
INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by)
VALUES ('HAIR', 'Hair type or colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACIAL_HAIR', 'Facial hair type', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('FACE', 'Face shape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('BUILD', 'Build', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('EYE', 'Eye colour', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('SMOKE', 'Smoker or vaper', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('MARK_TYPE', 'Type of identifying mark', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('SIDE', 'Side identifying mark is on', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('PART_ORIENT', 'Position of identifying mark', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART', 'Body part identifying mark is on', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER');

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
('SMOKE_NO', 'SMOKE', 'NO', 'No, they do not smoke or vape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
-- Distinguishing marks
('MARK_TYPE_MARK', 'MARK_TYPE', 'MARK', 'Mark', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('MARK_TYPE_OTH', 'MARK_TYPE', 'OTH', 'Other', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('MARK_TYPE_SCAR', 'MARK_TYPE', 'SCAR', 'Scar', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('MARK_TYPE_TAT', 'MARK_TYPE', 'TAT', 'Tattoo', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('SIDE_B', 'SIDE', 'B', 'Back', 3, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('SIDE_F', 'SIDE', 'F', 'Front', 4, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('SIDE_L', 'SIDE', 'L', 'Left', 2, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('SIDE_R', 'SIDE', 'R', 'Right', 1, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('SIDE_S', 'SIDE', 'S', 'Side', 5, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('PART_ORIENT_CENTR', 'PART_ORIENT', 'CENTR', 'Centre', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('PART_ORIENT_FACE', 'PART_ORIENT', 'FACE', 'Facing', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('PART_ORIENT_LOW', 'PART_ORIENT', 'LOW', 'Low', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('PART_ORIENT_UPP', 'PART_ORIENT', 'UPP', 'Upper', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_ANKLE', 'BODY_PART', 'ANKLE', 'Ankle', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_ARM', 'BODY_PART', 'ARM', 'Arm', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_EAR', 'BODY_PART', 'EAR', 'Ear', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_ELBOW', 'BODY_PART', 'ELBOW', 'Elbow', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_FACE', 'BODY_PART', 'FACE', 'Face', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_FINGER', 'BODY_PART', 'FINGER', 'Finger', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_FOOT', 'BODY_PART', 'FOOT', 'Foot', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_HAND', 'BODY_PART', 'HAND', 'Hand', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_HEAD', 'BODY_PART', 'HEAD', 'Head', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_KNEE', 'BODY_PART', 'KNEE', 'Knee', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_LEG', 'BODY_PART', 'LEG', 'Leg', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_LIP', 'BODY_PART', 'LIP', 'Lip', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_NECK', 'BODY_PART', 'NECK', 'Neck', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_NOSE', 'BODY_PART', 'NOSE', 'Nose', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_SHOULDER', 'BODY_PART', 'SHOULDER', 'Shoulder', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_THIGH', 'BODY_PART', 'THIGH', 'Thigh', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_TOE', 'BODY_PART', 'TOE', 'Toe', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
('BODY_PART_TORSO', 'BODY_PART', 'TORSO', 'Torso', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_domain (code, description, list_sequence, created_at, created_by, last_modified_at,
                                   last_modified_by, deactivated_at, deactivated_by)
VALUES ('INACTIVE', 'Inactive domain for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100',
        'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by, last_modified_at,
                                 last_modified_by, deactivated_at, deactivated_by)
VALUES ('FACE_INACTIVE', 'FACE', 'INACTIVE', 'Inactive code for tests', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER',
        '2024-07-11 17:00:00+0100', 'OMS_OWNER', '2024-07-11 17:00:00+0100', 'OMS_OWNER');

-- Smoker or vaper
INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('SMOKE_SMOKER', 'SMOKE', 'SMOKER', 'Yes, they smoke', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('SMOKE_VAPER', 'SMOKE', 'VAPER', 'Yes, they smoke or use nicotine replacement therapy (NRT)', 0,
        '2024-07-11 17:00:00+0100', 'OMS_OWNER'),
       ('SMOKE_NO', 'SMOKE', 'NO', 'No, they do not smoke or vape', 0, '2024-07-11 17:00:00+0100', 'OMS_OWNER');

-- Distinguishing marks
INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('MARK_TYPE_MARK', 'MARK_TYPE', 'MARK', 'Mark', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('MARK_TYPE_OTH', 'MARK_TYPE', 'OTH', 'Other', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('MARK_TYPE_SCAR', 'MARK_TYPE', 'SCAR', 'Scar', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('MARK_TYPE_TAT', 'MARK_TYPE', 'TAT', 'Tattoo', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('SIDE_B', 'SIDE', 'B', 'Back', 3, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('SIDE_F', 'SIDE', 'F', 'Front', 4, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('SIDE_L', 'SIDE', 'L', 'Left', 2, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('SIDE_R', 'SIDE', 'R', 'Right', 1, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('SIDE_S', 'SIDE', 'S', 'Side', 5, '2024-07-21 14:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('PART_ORIENT_CENTR', 'PART_ORIENT', 'CENTR', 'Centre', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('PART_ORIENT_FACE', 'PART_ORIENT', 'FACE', 'Facing', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('PART_ORIENT_LOW', 'PART_ORIENT', 'LOW', 'Low', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('PART_ORIENT_UPP', 'PART_ORIENT', 'UPP', 'Upper', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES ('BODY_PART_ANKLE', 'BODY_PART', 'ANKLE', 'Ankle', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_ARM', 'BODY_PART', 'ARM', 'Arm', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_EAR', 'BODY_PART', 'EAR', 'Ear', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_ELBOW', 'BODY_PART', 'ELBOW', 'Elbow', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_FACE', 'BODY_PART', 'FACE', 'Face', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_FINGER', 'BODY_PART', 'FINGER', 'Finger', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_FOOT', 'BODY_PART', 'FOOT', 'Foot', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_HAND', 'BODY_PART', 'HAND', 'Hand', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_HEAD', 'BODY_PART', 'HEAD', 'Head', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_KNEE', 'BODY_PART', 'KNEE', 'Knee', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_LEG', 'BODY_PART', 'LEG', 'Leg', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_LIP', 'BODY_PART', 'LIP', 'Lip', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_NECK', 'BODY_PART', 'NECK', 'Neck', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_NOSE', 'BODY_PART', 'NOSE', 'Nose', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_SHOULDER', 'BODY_PART', 'SHOULDER', 'Shoulder', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_THIGH', 'BODY_PART', 'THIGH', 'Thigh', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_TOE', 'BODY_PART', 'TOE', 'Toe', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER'),
       ('BODY_PART_TORSO', 'BODY_PART', 'TORSO', 'Torso', 0, '2024-07-21 14:00:00+0100', 'OMS_OWNER');