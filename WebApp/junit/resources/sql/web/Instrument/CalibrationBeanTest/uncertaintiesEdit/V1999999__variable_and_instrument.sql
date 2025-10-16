-- Test variable
INSERT INTO variables (id, name) VALUES (1000000, 'testVar');

-- Test sensor type
INSERT INTO sensor_types (id, name, vargroup, display_order) VALUES (1000000, 'testSensor', 'testGroup', 1000);

-- An unused sensor type
INSERT INTO sensor_types (id, name, vargroup, display_order) VALUES (1000001, 'Unused sensor', 'Unusded group', 1001);

-- Variables for Test - SST and EqT
INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'testVar'),
    1, 1, 3, 4
  );

INSERT INTO variable_sensors
  (variable_id, sensor_type, core, questionable_cascade, bad_cascade)
  VALUES (
    (SELECT id FROM variables WHERE name = 'testVar'),
    3, 0, 4, 3
  );

-- Benguela Stream instrument definition

-- Instrument
INSERT INTO instrument VALUES (1,1,'Uncertainty Instrument','Uncertainty Instrument','UNCI',0,NULL,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

INSERT INTO instrument_variables (instrument_id, variable_id)
  VALUES (1, (SELECT id FROM variables WHERE name = 'testVar'));

-- File definition
INSERT INTO file_definition VALUES
  (1,1,'Data File',' ',0,0,NULL,2,65,
   '{"valueColumn":7,"hemisphereColumn":8,"format":2}',
   '{"valueColumn":5,"hemisphereColumn":6,"format":1}',
   '{"assignments":{"0":{"assignmentIndex":0,"column":-1,"properties":{}},"1":{"assignmentIndex":1,"column":-1,"properties":{}},"2":{"assignmentIndex":2,"column":3,"properties":{"formatString":"dd/MM/yy"}},"3":{"assignmentIndex":3,"column":-1,"properties":{}},"4":{"assignmentIndex":4,"column":-1,"properties":{}},"5":{"assignmentIndex":5,"column":-1,"properties":{}},"6":{"assignmentIndex":6,"column":-1,"properties":{}},"7":{"assignmentIndex":7,"column":-1,"properties":{}},"8":{"assignmentIndex":8,"column":4,"properties":{"formatString":"HH:mm:ss"}},"9":{"assignmentIndex":9,"column":-1,"properties":{}},"10":{"assignmentIndex":10,"column":-1,"properties":{}},"11":{"assignmentIndex":11,"column":-1,"properties":{}},"12":{"assignmentIndex":12,"column":-1,"properties":{}}},"fileHasHeader":false}',
   '2019-01-28 13:31:21','2019-01-28 14:31:21');
   
-- Water temperature
INSERT INTO file_column VALUES (1,1,14,1,1,'SWTemp',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Eq
INSERT INTO file_column VALUES (3,1,63,1,3,'EqT',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');
