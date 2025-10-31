-- We create a cut down instrument so we don't have to set up sensors etc. for all the
-- sensor types defined for the instruments specified in resources/sql/testbase/instrument

-- Test variable
INSERT INTO variables (id, name) VALUES (1000000, 'testVar');

-- Test sensor type
INSERT INTO sensor_types (id, name, vargroup, display_order) VALUES (1000000, 'testSensor', 'testGroup', 1000);

-- An unused sensor type
INSERT INTO sensor_types (id, name, vargroup, display_order) VALUES (1000001, 'Unused sensor', 'Unusded group', 1001);

-- Variables for Test - The above test sensor and EqT
INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'testVar'),
    1000000, 1, 3, 4
  );

INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'testVar'),
    3, 0, 4, 3
  );

-- Instrument definition

-- Instrument
INSERT INTO instrument VALUES (3,1,'Uncertainty Instrument','Uncertainty Instrument','UNCI',0,NULL,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

INSERT INTO instrument_variables (instrument_id, variable_id)
  VALUES (3, (SELECT id FROM variables WHERE name = 'testVar'));

-- File definition
INSERT INTO file_definition VALUES
  (3,3,'Data File',' ',0,0,NULL,2,65,
   '{"valueColumn":7,"hemisphereColumn":8,"format":2}',
   '{"valueColumn":5,"hemisphereColumn":6,"format":1}',
   '{"assignments":{"0":{"assignmentIndex":0,"column":-1,"properties":{}},"1":{"assignmentIndex":1,"column":-1,"properties":{}},"2":{"assignmentIndex":2,"column":3,"properties":{"formatString":"dd/MM/yy"}},"3":{"assignmentIndex":3,"column":-1,"properties":{}},"4":{"assignmentIndex":4,"column":-1,"properties":{}},"5":{"assignmentIndex":5,"column":-1,"properties":{}},"6":{"assignmentIndex":6,"column":-1,"properties":{}},"7":{"assignmentIndex":7,"column":-1,"properties":{}},"8":{"assignmentIndex":8,"column":4,"properties":{"formatString":"HH:mm:ss"}},"9":{"assignmentIndex":9,"column":-1,"properties":{}},"10":{"assignmentIndex":10,"column":-1,"properties":{}},"11":{"assignmentIndex":11,"column":-1,"properties":{}},"12":{"assignmentIndex":12,"column":-1,"properties":{}}},"fileHasHeader":false}',
   '2019-01-28 13:31:21','2019-01-28 14:31:21');
   
-- Water temperature
INSERT INTO file_column VALUES (10000,3,14,1,1000000,'test_sensor',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Eq
INSERT INTO file_column VALUES (10001,3,63,1,3,'EqT',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');


-- Datasets

-- 2024-02-20 to 2024-03-10
INSERT INTO dataset
  (id, instrument_id, name, start, end, min_longitude, max_longitude, min_latitude, max_latitude, status, status_date, nrt, exported)
  VALUES
  (21, 3, 'D1', 1676851200000, 1678406400000, -10, 10, -10, 10, 50, 1718197125000, 0, 0);

-- 2024-05-10 to 2024-05-20
INSERT INTO dataset
  (id, instrument_id, name, start, end, min_longitude, max_longitude, min_latitude, max_latitude, status, status_date, nrt, exported)
  VALUES
  (22, 3, 'D2', 1683676800000, 1684540800000, -10, 10, -10, 10, 50, 1718197125000, 0, 0);

-- 2024-06-10 to 2024-06-20
INSERT INTO dataset
  (id, instrument_id, name, start, end, min_longitude, max_longitude, min_latitude, max_latitude, status, status_date, nrt, exported)
  VALUES
  (23, 3, 'D3', 1686355200000, 1687219200000, -10, 10, -10, 10, 50, 1718197125000, 0, 0);

-- 2024-07-10 to 2024-07-20
INSERT INTO dataset
  (id, instrument_id, name, start, end, min_longitude, max_longitude, min_latitude, max_latitude, status, status_date, nrt, exported)
  VALUES
  (24, 3, 'D4', 1688947200000, 1689811200000, -10, 10, -10, 10, 50, 1718197125000, 0, 0);
