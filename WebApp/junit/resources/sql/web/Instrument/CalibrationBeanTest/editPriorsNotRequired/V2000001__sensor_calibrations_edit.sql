-- A complete set of datasets and standards for edit testing

-- 2019-06-03T00:00:00 to 2019-06-05T00:00:00
INSERT INTO dataset (id, instrument_id, name, start, end, status, status_date)
  VALUES (1001, 1000000, 'A', 1559520000000, 1559692800000, 4, 0);

-- 2019-06-07T00:00:00 to 2019-06-09T00:00:00
INSERT INTO dataset (id, instrument_id, name, start, end, status, status_date)
  VALUES (1002, 1000000, 'B', 1559865600000, 1560038400000, 4, 0);

-- 2019-06-11T00:00:00 to 2019-06-13T00:00:00
INSERT INTO dataset (id, instrument_id, name, start, end, status, status_date)
  VALUES (1003, 1000000, 'C', 1560211200000, 1560384000000, 4, 0);

-- 2019-06-15T00:00:00 to 2019-06-17T00:00:00
INSERT INTO dataset (id, instrument_id, name, start, end, status, status_date)
  VALUES (1004, 1000000, 'D', 1560556800000, 1560729600000, 4, 0);


-- 2019-06-02T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1001, 1000000, 'SENSOR_CALIBRATION', '1001', 1559433600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-08T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1002, 1000000, 'SENSOR_CALIBRATION', '1001', 1559952000000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-13T01:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1003, 1000000, 'SENSOR_CALIBRATION', '1001', 1560387600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-13T12:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1004, 1000000, 'SENSOR_CALIBRATION', '1001', 1560427200000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-14T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1005, 1000000, 'SENSOR_CALIBRATION', '1001', 1560470400000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-19T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1006, 1000000, 'SENSOR_CALIBRATION', '1001', 1560902400000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-20T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1007, 1000000, 'SENSOR_CALIBRATION', '1001', 1560988800000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-01T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1008, 1000000, 'SENSOR_CALIBRATION', '1002', 1559347200000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-02T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1009, 1000000, 'SENSOR_CALIBRATION', '1002', 1559433600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-12T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1010, 1000000, 'SENSOR_CALIBRATION', '1002', 1560297600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-13T01:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1011, 1000000, 'SENSOR_CALIBRATION', '1002', 1560387600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-16T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1012, 1000000, 'SENSOR_CALIBRATION', '1002', 1560643200000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-16T12:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1013, 1000000, 'SENSOR_CALIBRATION', '1002', 1560686400000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-16T23:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1014, 1000000, 'SENSOR_CALIBRATION', '1002', 1560726000000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-20T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1015, 1000000, 'SENSOR_CALIBRATION', '1002', 1560988800000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-01T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1016, 1000000, 'SENSOR_CALIBRATION', '1003', 1559347200000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-04T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1017, 1000000, 'SENSOR_CALIBRATION', '1003', 1559606400000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-12T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1018, 1000000, 'SENSOR_CALIBRATION', '1003', 1560297600000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');

-- 2019-06-20T00:00:00
INSERT INTO calibration (id, instrument_id, type, target, deployment_date, coefficients, class)
  VALUES (1019, 1000000, 'SENSOR_CALIBRATION', '1003', 1560988800000, '0.0;0.0;0.0;0.0;1.0;0.0', 'PolynomialSensorCalibration');
