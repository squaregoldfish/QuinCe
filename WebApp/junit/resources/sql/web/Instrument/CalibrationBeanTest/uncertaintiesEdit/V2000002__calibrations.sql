-- Sensor Calibrations

-- 2023-02-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (21, 3, 'UNCERTAINTY', '10000', 1675209600000, '{"Type":"1","Value":"1"}', 'Uncertainty');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (22, 3, 'UNCERTAINTY', '10001', 1675209600000, '{"Type":"1","Value":"2"}', 'Uncertainty');

-- 2023-05-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (23, 3, 'UNCERTAINTY', '10001', 1682899200000, '{"Type":"1","Value":"3"}', 'Uncertainty');

-- 2023-07-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (24, 3, 'UNCERTAINTY', '10000', 1688169600000, '{"Type":"1","Value":"4"}', 'Uncertainty');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (25, 3, 'UNCERTAINTY', '10001', 1688169600000, '{"Type":"1","Value":"5"}', 'Uncertainty');
