package db_migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V55__HaganGenX_3029 extends BaseJavaMigration {

  private PreparedStatement sensorTypeStmt = null;

  private PreparedStatement variableSensorStmt = null;

  @Override
  public void migrate(Context context) throws Exception {

    // Setup
    Connection conn = context.getConnection();

    sensorTypeStmt = conn.prepareStatement("INSERT INTO sensor_types "
      + "(name, vargroup, display_order, column_code, column_heading, source_columns) "
      + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

    variableSensorStmt = conn.prepareStatement("INSERT INTO variable_sensors "
      + "(variable_id, sensor_type, core, questionable_cascade, bad_cascade) "
      + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

    // Add the base Variable
    PreparedStatement addVariableStmt = conn.prepareStatement(
      "INSERT INTO variables (name) VALUES ('Hagan GenX')",
      Statement.RETURN_GENERATED_KEYS);

    addVariableStmt.execute();

    ResultSet generatedKeys = addVariableStmt.getGeneratedKeys();
    generatedKeys.next();
    long variableID = generatedKeys.getLong(1);

    generatedKeys.close();
    addVariableStmt.close();

    /*
     * Store the preset run types. These are detected internally by the
     * specialised classes to handle the Hagan GenX. Therefore we also specify
     * that the user cannot select the Run Types.
     */
    String presetRunTypes = "{\"presetRunTypes\": [{\"runType\": [\"equ\", \"air\", \"zero\", \"span\"], \"category\": "
      + variableID + "}], \"userSelectableRunType\": false}";

    PreparedStatement runTypesStmt = conn
      .prepareStatement("UPDATE variables SET properties='" + presetRunTypes
        + "' WHERE id = " + variableID);

    runTypesStmt.execute();
    runTypesStmt.close();

    makeSensorType(variableID, "GenX Zero Timestamp", 15000, "GENXZEROTIME",
      "Zero Timestamp", "Zero Post Time", 0, 3, 4);
    makeSensorType(variableID, "GenX Zero CO₂ Raw 1", 15001, "GENXZEROCO2RAW1",
      "Zero CO₂ Raw 1", "ZERO_CO2Raw1_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Zero CO₂ Raw 2", 15002, "GENXZEROCO2RAW2",
      "Zero CO₂ Raw 2", "ZERO_CO2Raw2_AV", 0, 3, 4);

    makeSensorType(variableID, "GenX EQ CO₂ Raw 1", 100000, "GENXEQCO2RAW1",
      "EQ CO₂ Raw 1", "EQ_CO2Raw1_AV", 1, 3, 4);

    sensorTypeStmt.close();
    variableSensorStmt.close();

  }

  private void makeSensorType(long variableID, String name, int displayOrder,
    String columnCode, String columnHeading, String sourceColumn, int core,
    int questionableCascade, int badCascade) throws SQLException {

    sensorTypeStmt.setString(1, name);
    sensorTypeStmt.setString(2, "GenX");
    sensorTypeStmt.setInt(3, displayOrder);
    sensorTypeStmt.setString(4, columnCode);
    sensorTypeStmt.setString(5, columnHeading);
    sensorTypeStmt.setString(6, sourceColumn);

    sensorTypeStmt.execute();

    ResultSet keys = sensorTypeStmt.getGeneratedKeys();
    keys.next();
    long sensorTypeID = keys.getLong(1);
    keys.close();

    variableSensorStmt.setLong(1, variableID);
    variableSensorStmt.setLong(2, sensorTypeID);
    variableSensorStmt.setInt(3, core);
    variableSensorStmt.setInt(4, questionableCascade);
    variableSensorStmt.setInt(5, badCascade);

    variableSensorStmt.execute();
  }
}
