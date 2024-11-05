INSERT INTO field_history (field_history_id, prisoner_number, field, value_ref, value_json, created_at,
                           created_by,
                           applies_from, applies_to, source, anomalous)
VALUES (-201, 'A1234AA', 'SMOKER_OR_VAPER', 'SMOKE_SMOKER', null, '2024-01-02 09:10:11.123', 'USER1',
        '2024-01-02 09:10:11.123', NULL, 'DPS', false),
       (-202, 'A1234AA', 'MEDICAL_DIET', null, '{"field": "MEDICAL_DIET", "value": { "medicalDietaryRequirements": ["MEDICAL_DIET_LOW_FAT"] }}', '2024-01-02 09:10:11.123', 'USER1',
        '2024-01-02 09:10:11.123', NULL, 'DPS', false),
       (-203, 'A1234AA', 'FOOD_ALLERGY', null, '{"field": "FOOD_ALLERGY", "value": { "allergies": ["FOOD_ALLERGY_EGG"] }}', '2024-01-02 09:10:11.123', 'USER1',
        '2024-01-02 09:10:11.123', NULL, 'DPS', false);
