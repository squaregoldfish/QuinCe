-- Part two of the migration for non-time based measurements

-- Add foreign key reference to coordinate id
ALTER TABLE sensor_values ADD CONSTRAINT sv_coord FOREIGN KEY (coordinate_id) REFERENCES coordinates(id);

-- Remove old sensor_values columns
ALTER TABLE sensor_values DROP FOREIGN KEY SENSORVALUE_DATASET;
ALTER TABLE sensor_values DROP INDEX sv_datasetid_date;
ALTER TABLE sensor_values DROP COLUMN dataset_id;
ALTER TABLE sensor_values DROP COLUMN date;

-- Add measurement basis columns
-- All existing instruments and variables are surface based
ALTER TABLE instrument ADD basis TINYINT NOT NULL AFTER platform_code;
UPDATE instrument SET basis = 1;
ALTER TABLE variables ADD allowed_basis INT AFTER visible;
UPDATE variables SET allowed_basis = 1;