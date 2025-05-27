-- Part two of the migration for non-time based measurements

-- Add foreign key reference to coordinate id
SET SESSION foreign_key_checks = OFF;

ALTER TABLE sensor_values ADD CONSTRAINT sv_coord FOREIGN KEY (coordinate_id) REFERENCES coordinates(id), ALGORITHM = INPLACE, LOCK = NONE;
ALTER TABLE measurements ADD CONSTRAINT meas_coord FOREIGN KEY (coordinate_id) REFERENCES coordinates(id), ALGORITHM = INPLACE, LOCK = NONE;

-- Remove old sensor_values columns
ALTER TABLE sensor_values DROP FOREIGN KEY SENSORVALUE_DATASET, ALGORITHM = INPLACE, LOCK = NONE;
ALTER TABLE sensor_values DROP COLUMN dataset_id, ALGORITHM = INPLACE, LOCK = NONE;
ALTER TABLE sensor_values DROP COLUMN date, ALGORITHM = INPLACE, LOCK = NONE;

-- Remove old measurements columns
ALTER TABLE measurements DROP FOREIGN KEY measurement_dataset, ALGORITHM = INPLACE, LOCK = NONE;
ALTER TABLE measurements DROP COLUMN dataset_id, ALGORITHM = INPLACE, LOCK = NONE;
ALTER TABLE measurements DROP COLUMN date, ALGORITHM = INPLACE, LOCK = NONE;

-- Remove the original dataset start and end columns
ALTER TABLE dataset CHANGE start start_time BIGINT(20);
ALTER TABLE dataset CHANGE end end_time BIGINT(20);

SET SESSION foreign_key_checks = ON;

-- Add measurement basis columns
-- All existing instruments and variables are surface based
ALTER TABLE instrument ADD basis TINYINT NOT NULL AFTER platform_code;
UPDATE instrument SET basis = 1;
ALTER TABLE variables ADD allowed_basis INT AFTER visible;
UPDATE variables SET allowed_basis = 1;