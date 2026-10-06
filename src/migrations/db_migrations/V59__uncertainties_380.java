package db_migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintySerializer;

public class V59__uncertainties_380 extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {
    Connection conn = context.getConnection();

    // Add uncertainty column to SensorValues
    PreparedStatement addUncertaintyColStmt = conn.prepareStatement(
      "ALTER TABLE sensor_values ADD COLUMN uncertainty FLOAT DEFAULT NULL AFTER value");
    addUncertaintyColStmt.execute();
    addUncertaintyColStmt.close();

    // Replace NaN values (-9.999999999E8) with the one used by
    // DoubleWithUncertaintySerializer
    PreparedStatement measurementValuesNanValueStmt = conn.prepareStatement(
      "UPDATE measurements SET measurement_values = REGEXP_REPLACE(measurement_values, '\"value\":-9.999999999E8', '\"value\":"
        + DoubleWithUncertaintySerializer.NAN_DOUBLE + "')");
    measurementValuesNanValueStmt.execute();
    measurementValuesNanValueStmt.close();

    // Add uncertainties to measurement_values
    PreparedStatement measuremenetValuesStmt = conn.prepareStatement(
      "UPDATE measurements SET measurement_values = REGEXP_REPLACE(measurement_values, '\"value\":([^,]*),', '\"value\":[\\\\1,"
        + DoubleWithUncertaintySerializer.NAN_FLOAT + "],')");
    measuremenetValuesStmt.execute();
    measuremenetValuesStmt.close();

    // Data Reduction calculation_values
    PreparedStatement dataReductionStmt = conn.prepareStatement(
      "UPDATE data_reduction SET calculation_values = REGEXP_REPLACE(calculation_values, '\"([^\"]*)\":([^,}]*)', '\"\\\\1\":[\\\\2,"
        + DoubleWithUncertaintySerializer.NAN_FLOAT + "]')");
    dataReductionStmt.execute();
    dataReductionStmt.close();

  }
}
