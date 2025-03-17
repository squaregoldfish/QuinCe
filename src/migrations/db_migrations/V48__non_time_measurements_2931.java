package db_migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Migration for support for non-time-based data
 */
public class V48__non_time_measurements_2931 extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {
    Connection conn = context.getConnection();
    createCoordinates(conn);
  }

  private void createCoordinates(Connection conn) throws SQLException {

    // Create the coordinates table
    PreparedStatement createCoordsTableStatement = conn.prepareStatement(
      "CREATE TABLE coordinates (id BIGINT(20) NOT NULL AUTO_INCREMENT, "
        + "dataset_id INT(11) NOT NULL, date BIGINT(20) NULL, "
        + "depth MEDIUMINT NULL, station VARCHAR(10) NULL, "
        + "cast VARCHAR(10) NULL, bottle VARCHAR(10) NULL, "
        + "replicate VARCHAR(10) NULL, cycle VARCHAR(10) NULL, "
        + "PRIMARY KEY(id), INDEX coord_datasetid (dataset_id), "
        + "CONSTRAINT coord_dataset FOREIGN KEY (dataset_id) REFERENCES dataset(id))"
        + "ENGINE = InnoDB");

    createCoordsTableStatement.execute();
    createCoordsTableStatement.close();

    // Create the coordinates records by copying distinct dataset_id/date from
    // the sensor_values table
    PreparedStatement createCoordsStmt = conn.prepareStatement(
      "INSERT INTO coordinates (dataset_id, date) SELECT dataset_id, date FROM sensor_values GROUP BY dataset_id, date");
    createCoordsStmt.execute();
    conn.commit();
    createCoordsStmt.close();

    long maxCoordId;
    PreparedStatement maxCoordIdStmt = conn
      .prepareStatement("SELECT MAX(id) FROM coordinates");
    ResultSet maxCoordRecord = maxCoordIdStmt.executeQuery();
    maxCoordRecord.next();
    maxCoordId = maxCoordRecord.getLong(1);
    maxCoordRecord.close();
    maxCoordIdStmt.close();

    // Add coordinate field to sensor_values table with foreign key constraint
    PreparedStatement addSensorValuesCoordFieldStmt = conn.prepareStatement(
      "ALTER TABLE sensor_values ADD coordinate_id BIGINT(20) DEFAULT 0 NOT NULL AFTER id");
    addSensorValuesCoordFieldStmt.execute();
    addSensorValuesCoordFieldStmt.close();

    // Add coordinate field to measurements table with foreign key constraint
    PreparedStatement addMeasurementsCoordFieldStmt = conn.prepareStatement(
      "ALTER TABLE measurements ADD coordinate_id BIGINT(20) DEFAULT 0 NOT NULL AFTER id");
    addMeasurementsCoordFieldStmt.execute();
    addMeasurementsCoordFieldStmt.close();

    // Load all coordinate records and copy IDs back to sensor_values
    int batchSize = 20000;
    PreparedStatement getCoordsQuery = conn.prepareStatement(
      "SELECT id, dataset_id, date FROM coordinates WHERE id > ? ORDER BY id LIMIT "
        + batchSize);

    PreparedStatement writeSensorValuesCoordStmt = conn.prepareStatement(
      "UPDATE sensor_values SET coordinate_id = ? WHERE dataset_id = ? AND date = ?");

    PreparedStatement writeMeasurementsCoordStmt = conn.prepareStatement(
      "UPDATE measurements SET coordinate_id = ? WHERE dataset_id = ? AND date = ?");

    long lastCoordId = 0L;
    while (lastCoordId < maxCoordId) {
      getCoordsQuery.setLong(1, lastCoordId);

      // Copy coordinate IDs back to
      ResultSet coords = getCoordsQuery.executeQuery();
      while (coords.next()) {
        long coordId = coords.getLong(1);
        writeSensorValuesCoordStmt.setLong(1, coordId);
        writeSensorValuesCoordStmt.setLong(2, coords.getLong(2));
        writeSensorValuesCoordStmt.setLong(3, coords.getLong(3));
        writeSensorValuesCoordStmt.addBatch();

        writeMeasurementsCoordStmt.setLong(1, coordId);
        writeMeasurementsCoordStmt.setLong(2, coords.getLong(2));
        writeMeasurementsCoordStmt.setLong(3, coords.getLong(3));
        writeMeasurementsCoordStmt.addBatch();

        lastCoordId = coordId;
      }

      System.out.println(lastCoordId);
      writeSensorValuesCoordStmt.executeBatch();
      writeMeasurementsCoordStmt.executeBatch();
      conn.commit();
      coords.close();
    }

    getCoordsQuery.close();
    writeSensorValuesCoordStmt.close();
    writeMeasurementsCoordStmt.close();
  }
}
