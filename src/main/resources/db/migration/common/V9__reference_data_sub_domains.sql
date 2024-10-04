ALTER TABLE reference_data_domain
ADD COLUMN parent_domain_code VARCHAR(40) references reference_data_domain;

COMMENT ON COLUMN reference_data_domain.parent_domain_code IS 'Used for creating subdomains of other codes'
