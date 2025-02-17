-- Add basis field
ALTER TABLE instrument ADD basis TINYINT NOT NULL AFTER platform_code;

-- Update all existing instruments
UPDATE instrument SET basis = 1;
