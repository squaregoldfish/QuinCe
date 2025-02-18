-- Add basis field
ALTER TABLE instrument ADD basis TINYINT NOT NULL AFTER platform_code;

-- Update all existing instruments
UPDATE instrument SET basis = 1;

-- Add allowable basis to variables
ALTER TABLE variables ADD allowed_basis INT AFTER visible;

-- All existing variables are surface measurements
UPDATE variables SET allowed_basis = 1;

-- Now enforce NOT NULL
ALTER TABLE variables MODIFY allowed_basis INT NOT NULL;


-- Add First Oxygen Optode (likely to change)

-- Sensor types
INSERT INTO sensor_types (
    name, vargroup, display_order, column_code
  ) VALUES (
    'DOXY', 'Oxygen', 10000, 'DOXY'
  );

-- Variable
INSERT INTO variables (name, visible, allowed_basis) VALUES ('Aanderaa 4330 Oxygen', 1, 3);

-- Variable sensors
INSERT INTO variable_sensors (
    variable_id, sensor_type, core, questionable_cascade, bad_cascade
  )
  VALUES (
    (SELECT id FROM variables WHERE name = 'Aanderaa 4330 Oxygen'),
    (SELECT id FROM sensor_types WHERE name = 'DOXY'),
    1, 0, 0
  );

INSERT INTO variable_sensors (
    variable_id, sensor_type, core, questionable_cascade, bad_cascade
  )
  VALUES (
    (SELECT id FROM variables WHERE name = 'Aanderaa 4330 Oxygen'),
    (SELECT id FROM sensor_types WHERE name = 'Water Temperature'),
    0, 3, 4
  );

  
INSERT INTO variable_sensors (
    variable_id, sensor_type, core, questionable_cascade, bad_cascade
  )
  VALUES (
    (SELECT id FROM variables WHERE name = 'Aanderaa 4330 Oxygen'),
    (SELECT id FROM sensor_types WHERE name = 'Salinity'),
    0, 3, 4
  );
