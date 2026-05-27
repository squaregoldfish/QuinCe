package db_migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V59__HaganGenX_3029 extends BaseJavaMigration {

  private PreparedStatement sensorTypeStmt = null;

  private PreparedStatement variableSensorStmt = null;

  @Override
  public void migrate(Context context) throws Exception {

    // Setup
    Connection conn = context.getConnection();

    sensorTypeStmt = conn.prepareStatement("INSERT INTO sensor_types "
      + "(name, vargroup, display_order, column_code, column_heading, source_columns) "
      + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

    variableSensorStmt = conn.prepareStatement(
      "INSERT INTO variable_sensors "
        + "(variable_id, sensor_type, core, cascades) " + "VALUES (?, ?, ?, ?)",
      Statement.RETURN_GENERATED_KEYS);

    // Add the base Variable
    PreparedStatement addVariableStmt = conn.prepareStatement(
      "INSERT INTO variables (name, allowed_basis) VALUES ('Hagan GenX', 1)",
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

    // Zero
    makeSensorType(variableID, "GenX Zero Timestamp", 15000, "GENXZEROTIME",
      "Zero Timestamp", "Zero Post Time", 0, 3, 4);
    makeSensorType(variableID, "GenX Zero CO₂ Raw 1", 15001, "GENXZEROCO2RAW1",
      "Zero CO₂ Raw 1", "ZERO_CO2Raw1_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Zero CO₂ Raw 2", 15002, "GENXZEROCO2RAW2",
      "Zero CO₂ Raw 2", "ZERO_CO2Raw2_AV", 0, 3, 4);

    // Span
    makeSensorType(variableID, "GenX Span Timestamp", 15003, "GENXSPANTIME",
      "Span Timestamp", "Span Post Time", 0, 3, 4);
    makeSensorType(variableID, "GenX Span Temperature", 15004, "GENXSPANTEMP",
      "Span Temperature", "SPAN_CO2Temp_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Span Pressure", 15005, "GENXSPANPRES",
      "Span Pressure", "SPAN_CO2Pres_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Span CO₂ Raw 1", 15006, "GENXSPANCO2RAW1",
      "Span CO₂ Raw 1", "SPAN_CO2Raw1_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Span CO₂ Raw 2", 15007, "GENXSPANCO2RAW2",
      "Span CO₂ Raw 2", "SPAN_CO2Raw2_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Span Relative Humidity", 15008,
      "GENXSPANRH", "Span Relative Humidity", "SPAN_RhCalc_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Span Relative Humidity Temperature", 15009,
      "GENXSPANRHTEMP", "Span Relative Humidity Temperature", "SPAN_RhTemp_AV",
      0, 3, 4);

    // EQU (water measurements)
    // The water timestamp will be the timestamp of the main record
    makeSensorType(variableID, "GenX Water Temperature", 15010, "GENXWATERTEMP",
      "Water Temperature", "EQ_CO2Temp_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Water Pressure", 15011, "GENXWATERPRES",
      "Water Pressure", "EQ_CO2Pres_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Water CO₂ Raw 1", 15012,
      "GENXWATERCO2RAW1", "Water CO₂ Raw 1", "EQ_CO2Raw1_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Water CO₂ Raw 2", 15013,
      "GENXWATERCO2RAW2", "Water CO₂ Raw 2", "EQ_CO2Raw2_AV", 1, 3, 4);
    makeSensorType(variableID, "GenX Water Relative Humidity", 15014,
      "GENXWATERRH", "Water Relative Humidity", "EQ_RhCalc_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Water Relative Humidity Temperature",
      15015, "GENXWATERRHTEMP", "Water Relative Humidity Temperature",
      "EQ_RhTemp_AV", 0, 3, 4);

    // EQU (water measurements)
    makeSensorType(variableID, "GenX Air Timestamp", 15016, "GENXAIRTIME",
      "Air Timestamp", "Air Time", 0, 3, 4);
    makeSensorType(variableID, "GenX Air Temperature", 15017, "GENXAIRTEMP",
      "Air Temperature", "AIR_CO2Temp_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Air Pressure", 15018, "GENXAIRPRES",
      "Air Pressure", "AIR_CO2Pres_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Air CO₂ Raw 1", 15019, "GENXAIRCO2RAW1",
      "Air CO₂ Raw 1", "AIR_CO2Raw1_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Air CO₂ Raw 2", 15020, "GENXAIRCO2RAW2",
      "Air CO₂ Raw 2", "AIR_CO2Raw2_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Air Relative Humidity", 15021, "GENXAIRRH",
      "Air Relative Humidity", "AIR_RhCalc_AV", 0, 3, 4);
    makeSensorType(variableID, "GenX Air Relative Humidity Temperature", 15022,
      "GENXAIRRHTEMP", "Air Relative Humidity Temperature", "AIR_RhTemp_AV", 0,
      3, 4);

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
    variableSensorStmt.setString(4,
      "{\"Time\":[[3," + questionableCascade + "],[4," + badCascade + "]]}");

    variableSensorStmt.execute();
  }
}
