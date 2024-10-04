-- Seed data for tests
INSERT INTO reference_data_domain (code, parent_domain_code, description, list_sequence, created_at, created_by)
VALUES ('FREE_FROM', 'MEDICAL_DIET', 'Medical diet', 0, '2024-07-21 14:00:00+0100', 'CONNECT_DPS');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES
-- Hair
('FREE_FROM_MONOAMINE_OXIDASE_INHIBITORS', 'FREE_FROM', 'MONOAMINE_OXIDASE_INHIBITORS',
 'Any foods that interact with monoamine oxidase inhibitors', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_CHEESE', 'FREE_FROM', 'CHEESE', 'Cheese', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS');

