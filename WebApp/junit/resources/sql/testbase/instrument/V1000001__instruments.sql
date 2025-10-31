-- Underway pCO2 instrument definition

-- Instrument
INSERT INTO instrument VALUES (1,1,'Underway pCO2','Underway pCO2','UWPC',0,NULL,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Instrument uses the basic marine pCO2 variable
INSERT INTO instrument_variables (instrument_id, variable_id)
  VALUES (1, (SELECT id FROM variables WHERE name = 'Underway Marine pCO₂'));

-- File definition
INSERT INTO file_definition VALUES
  (1,1,'Data File',' ',0,0,NULL,2,65,
   '{"valueColumn":7,"hemisphereColumn":8,"format":2}',
   '{"valueColumn":5,"hemisphereColumn":6,"format":1}',
   '{"assignments":{"0":{"assignmentIndex":0,"column":-1,"properties":{}},"1":{"assignmentIndex":1,"column":-1,"properties":{}},"2":{"assignmentIndex":2,"column":3,"properties":{"formatString":"dd/MM/yy"}},"3":{"assignmentIndex":3,"column":-1,"properties":{}},"4":{"assignmentIndex":4,"column":-1,"properties":{}},"5":{"assignmentIndex":5,"column":-1,"properties":{}},"6":{"assignmentIndex":6,"column":-1,"properties":{}},"7":{"assignmentIndex":7,"column":-1,"properties":{}},"8":{"assignmentIndex":8,"column":4,"properties":{"formatString":"HH:mm:ss"}},"9":{"assignmentIndex":9,"column":-1,"properties":{}},"10":{"assignmentIndex":10,"column":-1,"properties":{}},"11":{"assignmentIndex":11,"column":-1,"properties":{}},"12":{"assignmentIndex":12,"column":-1,"properties":{}}},"fileHasHeader":false}',
   '2019-01-28 13:31:21','2019-01-28 14:31:21');

-- File Columns

-- Water temperature
INSERT INTO file_column VALUES (1,1,14,1,1,'SWTemp',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Salinity
INSERT INTO file_column VALUES (2,1,63,1,2,'SBSal',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Equilibrator Temperature
INSERT INTO file_column VALUES (3,1,19,1,3,'PT100_1',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Equilibrator Pressure (absolute)
INSERT INTO file_column VALUES (4,1,36,1,5,'Pres2',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- CO2
INSERT INTO file_column VALUES (5,1,40,1,9,'LCO2D',0,'','2019-01-28 13:31:21','2019-01-28 14:31:21');

-- Run Type
INSERT INTO file_column VALUES (6,1,2,1,-1,'Run Type',0,NULL,'2019-01-28 13:31:21','2019-01-28 14:31:21');


-- CONTROS Instrument

-- Instrument
INSERT INTO instrument VALUES (2,1,'CONTROS','CONTROS','CONT',0,NULL,'','2025-10-29 13:27:41','2025-10-29 13:27:41');

-- Instrument uses the CONTROS pCO2 variable
INSERT INTO instrument_variables (instrument_id, variable_id)
  VALUES (2, 6);
  
-- File definition
INSERT INTO file_definition VALUES
  (2,2,'Data File',' ',0,0,NULL,2,65,
   '{"valueColumn":7,"hemisphereColumn":8,"format":2}',
   '{"valueColumn":5,"hemisphereColumn":6,"format":1}',
   '{"assignments":{"0":{"assignmentIndex":0,"column":-1,"properties":{}},"1":{"assignmentIndex":1,"column":-1,"properties":{}},"2":{"assignmentIndex":2,"column":3,"properties":{"formatString":"dd/MM/yy"}},"3":{"assignmentIndex":3,"column":-1,"properties":{}},"4":{"assignmentIndex":4,"column":-1,"properties":{}},"5":{"assignmentIndex":5,"column":-1,"properties":{}},"6":{"assignmentIndex":6,"column":-1,"properties":{}},"7":{"assignmentIndex":7,"column":-1,"properties":{}},"8":{"assignmentIndex":8,"column":4,"properties":{"formatString":"HH:mm:ss"}},"9":{"assignmentIndex":9,"column":-1,"properties":{}},"10":{"assignmentIndex":10,"column":-1,"properties":{}},"11":{"assignmentIndex":11,"column":-1,"properties":{}},"12":{"assignmentIndex":12,"column":-1,"properties":{}}},"fileHasHeader":false}',
   '2019-01-28 13:31:21','2019-01-28 14:31:21');

-- File columns
INSERT INTO file_column VALUES (1000,2,3,1,1,'TEMP',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1001,2,6,1,2,'SAL',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1002,2,4,1,16,'P_pump',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1003,2,5,1,40,'p_NDIR',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1004,2,6,1,41,'p_in',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1005,2,8,1,15,'U_supply',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1006,2,9,1,35,'Zero',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1007,2,10,1,36,'Flush',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1008,2,12,1,37,'Runtime',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1009,2,13,1,33,'Signal_raw',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1010,2,14,1,34,'Signal_ref',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1011,2,15,1,13,'T_sensor',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1012,2,20,1,11,'T_control',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
INSERT INTO file_column VALUES (1013,2,21,1,38,'T_gas',0,NULL,'2025-10-29 13:27:41','2025-10-29 13:27:41');
