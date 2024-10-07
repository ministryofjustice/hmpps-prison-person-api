-- Seed data for tests
INSERT INTO reference_data_domain (code, parent_domain_code, description, list_sequence, created_at, created_by)
VALUES ('FREE_FROM', 'MEDICAL_DIET', 'Medical diet - Free from', 0, '2024-07-21 14:00:00+0100', 'CONNECT_DPS');

INSERT INTO reference_data_code (id, domain, code, description, list_sequence, created_at, created_by)
VALUES
-- Hair
('FREE_FROM_MONOAMINE_OXIDASE_INHIBITORS', 'FREE_FROM', 'MONOAMINE_OXIDASE_INHIBITORS',
 'Any foods that interact with monoamine oxidase inhibitors', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_CHEESE', 'FREE_FROM', 'CHEESE', 'Cheese', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_DAIRY', 'FREE_FROM', 'DAIRY', 'Dairy', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_EGG', 'FREE_FROM', 'EGG', 'Egg', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_FAT', 'FREE_FROM', 'FAT', 'Fat', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_FRIED_FOOD', 'FREE_FROM', 'FRIED_FOOD', 'Fried food', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_FISH', 'FREE_FROM', 'FISH', 'Fish', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_GARLIC', 'FREE_FROM', 'GARLIC', 'Garlic', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_LACTOSE', 'FREE_FROM', 'LACTOSE', 'Lactose', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_ONION', 'FREE_FROM', 'ONION', 'Onion', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_PORK', 'FREE_FROM', 'PORK', 'Pork', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS'),
('FREE_FROM_POTATO', 'FREE_FROM', 'POTATO', 'Potato', 0, '2024-07-11 17:00:00+0100', 'CONNECT_DPS');

