INSERT INTO field_history (field_history_id, prisoner_number, field, value_int, created_at, created_by, applies_from, applies_to, source)
VALUES
    --------------------
    -- Prisoner A1234AA
    --------------------
    -- Height:
    (-1, 'A1234AA', 'HEIGHT', 160, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'NOMIS'),
    (-2, 'A1234AA', 'HEIGHT', 161, '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00', '2024-01-10 00:00:00', 'NOMIS'),
    (-3, 'A1234AA', 'HEIGHT', 160, '2024-01-10 00:00:00', 'USER1', '2024-01-10 00:00:00', NULL, 'DPS'),

    -- Weight:
    (-4, 'A1234AA', 'WEIGHT', 59, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'NOMIS'),
    (-5, 'A1234AA', 'WEIGHT', 60, '2024-01-05 00:00:00', 'USER1', '2024-01-05 00:00:00', NULL, 'NOMIS'),

    ------------------------------------------------
    -- Prisoner B1234BB (to be merged into A1234AA):
    ------------------------------------------------
    -- Height:
    (-6, 'B1234BB', 'HEIGHT', 158, '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00', '2024-01-03 00:00:00', 'NOMIS'),
    (-7, 'B1234BB', 'HEIGHT', 159, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00', '2024-01-04 00:00:00', 'NOMIS'),

    -- Weight:
    (-8, 'B1234BB', 'WEIGHT', 57, '2024-01-02 00:00:00', 'USER1', '2024-01-02 00:00:00', '2024-01-03 00:00:00', 'NOMIS'),
    (-9, 'B1234BB', 'WEIGHT', 58, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00', '2024-01-04 00:00:00', 'NOMIS'),
    (-10, 'B1234BB', 'WEIGHT', 59, '2024-01-03 00:00:00', 'USER1', '2024-01-03 00:00:00', NULL, 'DPS'),

    -------------------------------------------
    -- Prisoner C1234CC (to be left untouched):
    -------------------------------------------
    (-11, 'C1234CC', 'HEIGHT', 180, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'NOMIS'),
    (-12, 'C1234CC', 'WEIGHT', 80, '2024-01-01 00:00:00', 'USER1', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'NOMIS')
;