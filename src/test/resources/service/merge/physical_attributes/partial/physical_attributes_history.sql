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
     FALSE)
;
