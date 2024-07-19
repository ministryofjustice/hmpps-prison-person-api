CREATE ROLE prison_person LOGIN PASSWORD '${database_password}';
GRANT USAGE ON SCHEMA prison_person TO prison_person;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA prison_person TO prison_person;
ALTER DEFAULT PRIVILEGES IN SCHEMA prison_person
    GRANT USAGE, SELECT ON SEQUENCES TO prison_person;
