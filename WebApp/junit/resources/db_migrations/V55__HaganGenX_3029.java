package resources.db_migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V55__HaganGenX_3029 extends BaseJavaMigration {

  private PreparedStatement variableStmt = null;

  private PreparedStatement runTypesStmt = null;

  private PreparedStatement sensorTypeStmt = null;

  private PreparedStatement variableSensorStmt = null;

  @Override
  public void migrate(Context context) throws Exception {

    // Setup
    Connection conn = context.getConnection();

    variableStmt = conn.prepareStatement(
      "INSERT INTO variables (name) VALUES (?)",
      Statement.RETURN_GENERATED_KEYS);

    runTypesStmt = conn
      .prepareStatement("UPDATE variables SET properties = ?  WHERE id = ?");

    sensorTypeStmt = conn.prepareStatement("INSERT INTO sensor_types "
      + "(name, vargroup, display_order, internal_calibration, run_type_aware, column_code, "
      + " column_heading, source_columns) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
      Statement.RETURN_GENERATED_KEYS);

    variableSensorStmt = conn.prepareStatement("INSERT INTO variable_sensors "
      + "(variable_id, sensor_type, core, questionable_cascade, bad_cascade) "
      + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

    // Add the base Variable
    long waterVarId = makeVariable("Hagan GenX Water", "EQ");
    long airVarId = makeVariable("Hagan GenX Air", "AIR");

    long[] varIDs = new long[] { waterVarId, airVarId };

    makeSensorType(varIDs, "Status Code", "Other", 9001, 0, 0, "STATUSCODE",
      "Status Code", "StatusCode", 0, 3, 4);

    makeSensorType(varIDs, "Serial Number", "Other", 9002, 0, 0, "SERIALNUMBER",
      "Serial Number", "Serial Number", 0, 3, 4);

    makeSensorType(varIDs, "GenX Span Slope", "GenX", 1400, 0, 0,
      "GENXSPANSLOPE", "Span Slope", "Span Slope", 0, 3, 4);

    makeSensorType(varIDs, "GenX Temperature", "GenX", 1401, 0, 0, "GENXTEMP",
      "GenX Temperature", "CO2Temp_AV", 0, 3, 4);

    makeSensorType(varIDs, "GenX Pressure", "GenX", 1402, 0, 0, "GENXPRES",
      "GenX Pressure", "CO2Pres_AV", 0, 3, 4);

    makeSensorType(varIDs, "GenX CO₂ Raw 1", "GenX", 1403, 1, 1, "GENXCO2RAW1",
      "GenX CO₂ Raw 1", "CO2Raw1_AV", 1, 3, 4);

    makeSensorType(varIDs, "GenX CO₂ Raw 2", "GenX", 1404, 0, 1, "GENXCO2RAW2",
      "GenX CO₂ Raw 2", "CO2Raw2_AV", 0, 3, 4);

    makeSensorType(varIDs, "GenX Relative Humidity", "GenX", 1405, 0, 0,
      "GENXRH", "GenX Relative Humidity", "RhCalc_AV", 0, 3, 4);

    makeSensorType(varIDs, "GenX Relative Humidity Temperature", "GenX", 1406,
      0, 0, "GENXRHTEMP", "GenX Relative Humidity Temperature", "RhTemp_AV", 0,
      3, 4);

    makeSensorType(varIDs, "GenX CALK", "GenX", 1407, 0, 1, "GENXCALK",
      "GenX CALK", "CALK", 0, 3, 4);

    variableSensorStmt.close();
    sensorTypeStmt.close();
    runTypesStmt.close();
    variableStmt.close();
  }

  private long makeVariable(String varName, String runType)
    throws SQLException {

    ResultSet generatedKeys = null;
    long variableID = -1L;

    try {
      variableStmt.setString(1, varName);
      variableStmt.execute();

      generatedKeys = variableStmt.getGeneratedKeys();
      generatedKeys.next();
      variableID = generatedKeys.getLong(1);

      String properties = "{\"presetRunTypes\": [{\"runType\": [\"zero\"], \"category\": -3}, "
        + "{\"runType\": [\"span\"], \"category\": -3}, " + "{\"runType\": [\""
        + runType.toLowerCase() + "\"], \"category\": " + variableID + "}], "
        + "\"forceMeasurementMode\": \"continuous\"}";

      runTypesStmt.setString(1, properties);
      runTypesStmt.setLong(2, variableID);

      runTypesStmt.execute();
    } finally {
      if (null != generatedKeys) {
        generatedKeys.close();
      }
    }

    return variableID;
  }

  private void makeSensorType(long[] variableIDs, String name, String varGroup,
    int displayOrder, int internalCalibration, int runTypeAware,
    String columnCode, String columnHeading, String sourceColumn, int core,
    int questionableCascade, int badCascade) throws SQLException {

    sensorTypeStmt.setString(1, name);
    sensorTypeStmt.setString(2, varGroup);
    sensorTypeStmt.setInt(3, displayOrder);
    sensorTypeStmt.setInt(4, internalCalibration);
    sensorTypeStmt.setInt(5, runTypeAware);
    sensorTypeStmt.setString(6, columnCode);
    sensorTypeStmt.setString(7, columnHeading);
    sensorTypeStmt.setString(8, sourceColumn);

    sensorTypeStmt.execute();

    ResultSet keys = sensorTypeStmt.getGeneratedKeys();
    keys.next();
    long sensorTypeID = keys.getLong(1);
    keys.close();

    for (long varID : variableIDs) {
      variableSensorStmt.setLong(1, varID);
      variableSensorStmt.setLong(2, sensorTypeID);
      variableSensorStmt.setInt(3, core);
      variableSensorStmt.setInt(4, questionableCascade);
      variableSensorStmt.setInt(5, badCascade);

      variableSensorStmt.execute();
    }
  }
}
