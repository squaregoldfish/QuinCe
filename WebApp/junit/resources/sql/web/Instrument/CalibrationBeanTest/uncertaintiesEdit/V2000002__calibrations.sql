-- Sensor Calibrations

-- 2023-02-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (1, 1, 'UNCERTAINTY', '1', 1675209600000, '{"Type":"1","Value":"1"}', 'Uncertainty');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (2, 1, 'UNCERTAINTY', '3', 1675209600000, '{"Type":"1","Value":"2"}', 'Uncertainty');

-- 2023-05-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (3, 1, 'UNCERTAINTY', '3', 1682899200000, '{"Type":"1","Value":"3"}', 'Uncertainty');

-- 2023-07-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (4, 1, 'UNCERTAINTY', '1', 1688169600000, '{"Type":"1","Value":"4"}', 'Uncertainty');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (5, 1, 'UNCERTAINTY', '3', 1688169600000, '{"Type":"1","Value":"5"}', 'Uncertainty');
