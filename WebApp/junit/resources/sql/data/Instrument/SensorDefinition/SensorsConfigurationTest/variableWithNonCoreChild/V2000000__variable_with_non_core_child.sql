-- A test variable
INSERT INTO variables (name, allowed_basis) VALUES ('Has Child', 1);

-- A valid core sensor
INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'Has Child'),
    (SELECT id FROM sensor_types WHERE name = 'Salinity'),
    1, 3, 4
  );

-- Non-Core sensor that is a child
INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'Has Child'),
    (SELECT id FROM sensor_types WHERE name = 'Equilibrator Pressure (absolute)'),
    0, 3, 4
  );
