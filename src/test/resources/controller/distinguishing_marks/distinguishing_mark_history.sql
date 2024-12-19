INSERT INTO distinguishing_mark_history (distinguishing_mark_id, value_json, created_at, created_by, applies_from,
                                         applies_to, source, anomalous)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242257',
        '{ "prisonerNumber": "12345", "bodyPart": { "id": "BODY_PART_FACE", "description": "Face", "listSequence": "0", "isActive": true }, "markType": { "id": "MARK_TYPE_SCAR", "description": "Scar", "listSequence": "0", "isActive": true }, "side": { "id": "SIDE_R", "description": "Right", "listSequence": "1", "isActive": true }, "partOrientation": { "id": "PART_ORIENT_CENTR", "description": "Centre", "listSequence": "0", "isActive": true }, "comment": "Large scar from fight", "createdAt": "2024-01-02@09:10:11.123+0000", "createdBy": "USER_GEN", "photographUuids": [] }',
        '2024-01-02 09:10:11.123', 'USER1',
        '2024-01-02 09:10:11.123', NULL, 'DPS', false);

INSERT INTO distinguishing_mark_history (distinguishing_mark_id, value_json, created_at, created_by, applies_from,
                                         applies_to, source, anomalous)
VALUES ('c46d0ce9-e586-4fa6-ae76-52ea8c242258',
        '{ "prisonerNumber": "12345", "bodyPart": { "id": "BODY_PART_FACE", "description": "Face", "listSequence": "0", "isActive": true }, "markType": { "id": "MARK_TYPE_SCAR", "description": "Scar", "listSequence": "0", "isActive": true }, "side": { "id": "SIDE_R", "description": "Right", "listSequence": "1", "isActive": true }, "partOrientation": { "id": "PART_ORIENT_CENTR", "description": "Centre", "listSequence": "0", "isActive": true }, "comment": "Another scar", "createdAt": "2024-01-02@09:10:11.123+0000", "createdBy": "USER_GEN", "photographUuids": [{ "id": "c46d0ce9-e586-4fa6-ae76-52ea8c242260", "latest": false }, { "id": "c46d0ce9-e586-4fa6-ae76-52ea8c242261", "latest": true }] }',
        '2024-01-02 09:10:11.123', 'USER1',
        '2024-01-02 09:10:11.123', NULL, 'DPS', false);
