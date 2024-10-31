INSERT INTO field_history (field_history_id, prisoner_number, field, value_int, value_string, value_ref, created_at,
                           created_by, applies_from,
                           applies_to, source, anomalous)
VALUES
    --------------------
    -- Prisoner A1234AA
    --------------------
    -- Height:
    (-1, 'A1234AA', 'HEIGHT', 160, NULL, NULL, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-2, 'A1234AA', 'HEIGHT', 161, NULL, NULL, '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-3, 'A1234AA', 'HEIGHT', 160, NULL, NULL, '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL, 'DPS',
     FALSE),

    -- Weight:
    (-4, 'A1234AA', 'WEIGHT', 59, NULL, NULL, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00', 'NOMIS',
     FALSE),
    (-5, 'A1234AA', 'WEIGHT', 60, NULL, NULL, '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00', NULL, 'NOMIS',
     FALSE),

    -- Hair:
    (-101, 'A1234AA', 'HAIR', NULL, NULL, 'HAIR_BLONDE', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-102, 'A1234AA', 'HAIR', NULL, NULL, 'HAIR_BLACK', '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-103, 'A1234AA', 'HAIR', NULL, NULL, 'HAIR_DARK', '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Facial hair:
    (-201, 'A1234AA', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_BEARDED', '2024-01-01 00:00:00', 'USER1',
     '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-202, 'A1234AA', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_CLEAN SHAVEN', '2024-01-05 00:00:00', 'USER1',
     '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-203, 'A1234AA', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_GOATEE', '2024-01-10 00:00:00', 'USER1',
     '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Face:
    (-301, 'A1234AA', 'FACE', NULL, NULL, 'FACE_ANGULAR', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-302, 'A1234AA', 'FACE', NULL, NULL, 'FACE_BULLET', '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-303, 'A1234AA', 'FACE', NULL, NULL, 'FACE_OVAL', '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Build:
    (-401, 'A1234AA', 'BUILD', NULL, NULL, 'BUILD_FRAIL', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-402, 'A1234AA', 'BUILD', NULL, NULL, 'BUILD_HEAVY', '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-403, 'A1234AA', 'BUILD', NULL, NULL, 'BUILD_MEDIUM', '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Left eye colour:
    (-501, 'A1234AA', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_BLUE', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-502, 'A1234AA', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_BROWN', '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-503, 'A1234AA', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_CLOUDED', '2024-01-10 00:00:00', 'USER1',
     '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Right eye colour:
    (-601, 'A1234AA', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_BLUE', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-602, 'A1234AA', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_BROWN', '2024-01-05 00:00:00', 'USER1',
     '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-603, 'A1234AA', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_CLOUDED', '2024-01-10 00:00:00', 'USER1',
     '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    -- Shoe size:
    (-701, 'A1234AA', 'SHOE_SIZE', NULL, '8', NULL, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-702, 'A1234AA', 'SHOE_SIZE', NULL, '9', NULL, '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00',
     '2024-01-10 00:00:00',
     'NOMIS', FALSE),
    (-703, 'A1234AA', 'SHOE_SIZE', NULL, '10', NULL, '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL,
     'DPS',
     FALSE),

    ------------------------------------------------
    -- Prisoner B1234BB (to be merged into A1234AA):
    ------------------------------------------------
    -- Height:
    (-6, 'B1234BB', 'HEIGHT', 158, NULL, NULL, '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-7, 'B1234BB', 'HEIGHT', 159, NULL, NULL, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     '2024-01-04 00:00:00',
     'NOMIS', FALSE),

    -- Weight:
    (-8, 'B1234BB', 'WEIGHT', 57, NULL, NULL, '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00', 'NOMIS',
     FALSE),
    (-9, 'B1234BB', 'WEIGHT', 58, NULL, NULL, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     '2024-01-04 00:00:00', 'NOMIS',
     FALSE),
    (-10, 'B1234BB', 'WEIGHT', 59, NULL, NULL, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00', NULL, 'DPS',
     FALSE),

    -- Hair:
    (-104, 'B1234BB', 'HAIR', NULL, NULL, 'HAIR_DYED', '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-105, 'B1234BB', 'HAIR', NULL, NULL, 'HAIR_LIGHT', '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Facial hair:
    (-204, 'B1234BB', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_MOUSTACHE', '2024-01-02 00:00:00', 'USER1',
     '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-205, 'B1234BB', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_SIDEBURNS', '2024-01-03 00:00:00', 'USER1',
     '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Face:
    (-304, 'B1234BB', 'FACE', NULL, NULL, 'FACE_ROUND', '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-305, 'B1234BB', 'FACE', NULL, NULL, 'FACE_SQUARE', '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Build:
    (-404, 'B1234BB', 'BUILD', NULL, NULL, 'BUILD_MUSC', '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-405, 'B1234BB', 'BUILD', NULL, NULL, 'BUILD_OBESE', '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Left eye colour:
    (-504, 'B1234BB', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_GREEN', '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-505, 'B1234BB', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_GREY', '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Right eye colour:
    (-604, 'B1234BB', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_GREEN', '2024-01-02 00:00:00', 'USER1',
     '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-605, 'B1234BB', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_GREY', '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -- Shoe size:
    (-704, 'B1234BB', 'SHOE_SIZE', NULL, '11', NULL, '2024-01-02 00:00:00', 'USER1',
     '2024-01-02 00:00:00',
     '2024-01-03 00:00:00',
     'NOMIS', FALSE),
    (-705, 'B1234BB', 'SHOE_SIZE', NULL, '12', NULL, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00',
     NULL,
     'NOMIS', FALSE),

    -------------------------------------------
    -- Prisoner C1234CC (to be left untouched):
    -------------------------------------------
    (-11, 'C1234CC', 'HEIGHT', 180, NULL, NULL, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-12, 'C1234CC', 'WEIGHT', 80, NULL, NULL, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-106, 'C1234CC', 'HAIR', NULL, NULL, 'HAIR_GINGER', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-206, 'C1234CC', 'FACIAL_HAIR', NULL, NULL, 'FACIAL_HAIR_BEARDED', '2024-01-01 00:00:00', 'USER1',
     '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-306, 'C1234CC', 'FACE', NULL, NULL, 'FACE_TRIANGULAR', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-406, 'C1234CC', 'BUILD', NULL, NULL, 'BUILD_PROP', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-506, 'C1234CC', 'LEFT_EYE_COLOUR', NULL, NULL, 'EYE_HAZEL', '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-606, 'C1234CC', 'RIGHT_EYE_COLOUR', NULL, NULL, 'EYE_HAZEL', '2024-01-01 00:00:00', 'USER1',
     '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE),
    (-706, 'C1234CC', 'SHOE_SIZE', NULL, '6', NULL, '2024-01-01 00:00:00', 'USER1',
     '2024-01-01 00:00:00',
     '2024-01-02 00:00:00',
     'NOMIS', FALSE)
;
