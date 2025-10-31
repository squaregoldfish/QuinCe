-- Calculation Coefficients

-- Fixed coefficients

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (100, 2, 'CALC_COEFFICIENT', '6.k1', 1675209600000, '{"Value":"4.976303e-02"}', 'CalculationCoefficient');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (101, 2, 'CALC_COEFFICIENT', '6.k2', 1675209600000, '{"Value":"2.799624e-06"}', 'CalculationCoefficient');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (102, 2, 'CALC_COEFFICIENT', '6.k3', 1675209600000, '{"Value":"1.307428e-10"}', 'CalculationCoefficient');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (103, 2, 'CALC_COEFFICIENT', '6.Response Time', 1675209600000, '{"Value":"65"}', 'CalculationCoefficient');


-- 2023-02-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (1, 2, 'CALC_COEFFICIENT', '6.F', 1675209600000, '{"Value":"65001"}', 'CalculationCoefficient');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (2, 2, 'CALC_COEFFICIENT', '6.Runtime', 1675209600000, '{"Value":"50"}', 'CalculationCoefficient');

-- 2023-05-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (3, 2, 'CALC_COEFFICIENT', '6.Runtime', 1682899200000, '{"Value":"65002"}', 'CalculationCoefficient');

-- 2023-07-01
INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (4, 2, 'CALC_COEFFICIENT', '6.F', 1688169600000, '{"Value":"51"}', 'CalculationCoefficient');

INSERT INTO calibration
  (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES
  (5, 2, 'CALC_COEFFICIENT', '6.Runtime', 1688169600000, '{"Value":"52"}', 'CalculationCoefficient');
