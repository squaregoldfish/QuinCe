-- Uncertainty for Sensor Values
ALTER TABLE sensor_values ADD COLUMN uncertainty FLOAT DEFAULT NULL AFTER value;