-- Coordinates for Argo data

-- Drop the old cycle field
ALTER TABLE coordinates DROP COLUMN cycle;

-- Add the new fields
ALTER TABLE coordinates ADD cycle_number INT(3);
ALTER TABLE coordinates ADD nprof INT(2);
ALTER TABLE coordinates ADD direction CHAR(1);
ALTER TABLE coordinates ADD nlevel INT(3);
ALTER TABLE coordinates ADD pref DOUBLE;
ALTER TABLE coordinates ADD source_file VARCHAR(25);
