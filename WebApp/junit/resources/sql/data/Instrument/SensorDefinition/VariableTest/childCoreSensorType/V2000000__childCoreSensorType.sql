-- Second test variable
INSERT INTO variables (id, name, allowed_basis) VALUES (2000000, 'childCoreVar', 1);

INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    2000000,
    (SELECT id FROM sensor_types WHERE name = 'Equilibrator Pressure (differential)'),
    1, 3, 4
  );
