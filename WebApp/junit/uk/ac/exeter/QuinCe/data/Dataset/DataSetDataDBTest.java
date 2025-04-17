package uk.ac.exeter.QuinCe.data.Dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Dataset.QC.RoutineFlag;
import uk.ac.exeter.QuinCe.data.Dataset.QC.SensorValues.AutoQCResult;
import uk.ac.exeter.QuinCe.data.Dataset.QC.SensorValues.RangeCheckRoutine;
import uk.ac.exeter.QuinCe.utils.DatabaseUtils;
import uk.ac.exeter.QuinCe.utils.MissingParamException;

public class DataSetDataDBTest extends BaseTest {

  private static final long DATASET_ID = 1L;

  private static final long COLUMN_ID = 1L;

  private Connection conn = null;

  @BeforeEach
  public void setup() throws SQLException {
    initResourceManager();
    conn = getConnection(false);
  }

  @AfterEach
  public void tearDown() {
    DatabaseUtils.closeConnection(conn);
  }

  /**
   * Store a single SensorValue in the database, and retrieve it.
   * <p>
   * Also ensures that the single stored value is the only one returned.
   * </p>
   *
   * @param sensorValue
   *          The sensor value to be stored
   * @return The value retrieved from the database
   * @throws Exception
   */
  private SensorValue retrieveSingleStoredValue(Coordinate coordinate)
    throws Exception {

    DataSet dataset = DataSetDB.getDataSet(conn, DATASET_ID);

    DatasetSensorValues storedValues = DataSetDataDB.getSensorValues(conn,
      dataset, false, false);

    assertEquals(1, storedValues.size());

    SensorValuesList columnValues = storedValues.getColumnValues(COLUMN_ID);

    assertEquals(1, columnValues.rawSize());
    return columnValues.getRawSensorValue(coordinate);
  }

  /**
   * Test that a simple value can be stored, that its data is in the database,
   * and that the object is given a database ID.
   *
   * @throws Exception
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesSingleValueTest() throws Exception {

    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));
    String value = "20";
    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      value);

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    conn.commit();
    SensorValue storedValue = retrieveSingleStoredValue(coordinate);

    // Show that the SensorValue's ID has been updated
    assertNotEquals(DatabaseUtils.NO_DATABASE_RECORD, sensorValue.getId(),
      "Sensor Value ID was not updated");

    // Check the dirty flag
    assertFalse(sensorValue.isDirty(), "Dirty flag not cleared");

    assertNotEquals(DatabaseUtils.NO_DATABASE_RECORD, storedValue.getId(),
      "Database ID not set");
    assertEquals(COLUMN_ID, storedValue.getColumnId(), "Incorrect column ID");
    assertEquals(coordinate, storedValue.getCoordinate(), "Incorrect time");
    assertEquals(value, storedValue.getValue(), "Incorrect value");
    assertEquals(new AutoQCResult(), storedValue.getAutoQcResult(),
      "Auto QC result not stored correctly");
    assertEquals(Flag.ASSUMED_GOOD, storedValue.getUserQCFlag(),
      "Incorrect user QC flag");
    assertEquals("", storedValue.getUserQCMessage(),
      "Incorrect user QC message");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesNullValueTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));
    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      null);

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    conn.commit();

    SensorValue storedValue = retrieveSingleStoredValue(coordinate);

    assertNull(storedValue.getValue(), "Stored value not null");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesCustomUserQCValueTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      "20");

    Flag qcFlag = Flag.QUESTIONABLE;
    String qcMessage = "I question this value";
    sensorValue.setUserQC(qcFlag, qcMessage);

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    conn.commit();
    SensorValue storedValue = retrieveSingleStoredValue(coordinate);

    assertEquals(qcFlag, storedValue.getUserQCFlag(), "Incorrect user QC flag");
    assertEquals(qcMessage, storedValue.getUserQCMessage(),
      "Incorrect user QC message");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void updateSensorValueTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));
    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      "20");

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    conn.commit();

    SensorValue originalStoredValue = retrieveSingleStoredValue(coordinate);

    Flag userQCFlag = Flag.QUESTIONABLE;
    String userQCMessage = "Updated User QC";

    originalStoredValue.addAutoQCFlag(
      new RoutineFlag(new RangeCheckRoutine(), Flag.BAD, "77", "88"));
    originalStoredValue.setUserQC(userQCFlag, userQCMessage);
    AutoQCResult autoQC = originalStoredValue.getAutoQcResult();

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(originalStoredValue));
    conn.commit();

    // Check that the dirty flag is cleared
    assertFalse(originalStoredValue.isDirty(), "Dirty flag not cleared");

    SensorValue updatedValue = retrieveSingleStoredValue(coordinate);

    assertEquals(autoQC, updatedValue.getAutoQcResult(), "Incorrect Auto QC");
    assertEquals(userQCFlag, updatedValue.getUserQCFlag(),
      "Incorrect user QC flag");
    assertEquals(userQCMessage, updatedValue.getUserQCMessage(),
      "Incorrect user QC message");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesNoConnTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      "20");

    assertThrows(MissingParamException.class, () -> {
      DataSetDataDB.storeSensorValues(null, Arrays.asList(sensorValue));
    });
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesEmptyListTest() throws Exception {
    DataSetDataDB.storeSensorValues(conn, new ArrayList<SensorValue>());
    conn.commit();
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesNullListTest() throws Exception {
    assertThrows(MissingParamException.class, () -> {
      DataSetDataDB.storeSensorValues(conn, null);
    });
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesInvalidDatasetTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue sensorValue = new SensorValue(7000L, COLUMN_ID, coordinate,
      "20");

    assertThrows(InvalidSensorValueException.class, () -> {
      DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    });
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeSensorValuesInvalidColumnTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue sensorValue = new SensorValue(DATASET_ID, 7000L, coordinate,
      "20");

    assertThrows(InvalidSensorValueException.class, () -> {
      DataSetDataDB.storeSensorValues(conn, Arrays.asList(sensorValue));
    });
  }

  /**
   * Test that no values are stored if any values are invalid.
   *
   * @throws Exception
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void storeValuesMultipleValuesOneInvalid() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue sensorValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      "20");
    SensorValue badValue = new SensorValue(7000L, COLUMN_ID, coordinate, "20");

    boolean exceptionThrown = false;
    try {
      DataSetDataDB.storeSensorValues(conn,
        Arrays.asList(sensorValue, badValue));
    } catch (InvalidSensorValueException e) {
      conn.rollback();
      exceptionThrown = true;
    }

    assertTrue(exceptionThrown, "Expected InvalidSensorValueException");

    assertEquals(0,
      DataSetDataDB.getSensorValues(conn,
        DataSetDB.getDataSet(conn, DATASET_ID), false, false).size(),
      "Value has been stored; should not have been");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void getSensorValuesFlushingNotIgnoredTest() throws Exception {
    TimeCoordinate coordinate1 = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));
    TimeCoordinate coordinate2 = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 1));

    SensorValue normalValue = new SensorValue(DATASET_ID, COLUMN_ID,
      coordinate1, "20");

    SensorValue flushingValue = new SensorValue(DATASET_ID, COLUMN_ID,
      coordinate2, "21");
    flushingValue.setUserQC(Flag.FLUSHING, "Flushing");

    DataSetDataDB.storeSensorValues(conn,
      Arrays.asList(normalValue, flushingValue));
    conn.commit();

    assertEquals(2,
      DataSetDataDB.getSensorValues(conn,
        DataSetDB.getDataSet(conn, DATASET_ID), false, false).size(),
      "Incorrect number of values retrieved");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void getSensorValuesFlushingIgnoredTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue normalValue = new SensorValue(DATASET_ID, COLUMN_ID, coordinate,
      "20");

    SensorValue flushingValue = new SensorValue(DATASET_ID, COLUMN_ID,
      coordinate, "21");
    flushingValue.setUserQC(Flag.FLUSHING, "Flushing");

    DataSetDataDB.storeSensorValues(conn,
      Arrays.asList(normalValue, flushingValue));
    conn.commit();

    assertEquals(1,
      DataSetDataDB.getSensorValues(conn,
        DataSetDB.getDataSet(conn, DATASET_ID), true, false).size(),
      "Incorrect number of values retrieved");
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/variable", "resources/sql/testbase/instrument",
    "resources/sql/testbase/dataset" })
  @Test
  public void deleteSensorValuesTest() throws Exception {
    TimeCoordinate coordinate = new TimeCoordinate(DATASET_ID,
      LocalDateTime.of(2021, 1, 1, 0, 0, 0));

    SensorValue value1 = new SensorValue(DATASET_ID, 1L, coordinate, "20");

    SensorValue value2 = new SensorValue(DATASET_ID, 2L, coordinate, "21");

    DataSetDataDB.storeSensorValues(conn, Arrays.asList(value1, value2));
    conn.commit();

    assertEquals(2,
      DataSetDataDB.getSensorValues(conn,
        DataSetDB.getDataSet(conn, DATASET_ID), true, false).size(),
      "Values not stored as expected");

    DataSetDataDB.deleteSensorValues(getConnection(), DATASET_ID);

    assertEquals(0,
      DataSetDataDB.getSensorValues(conn,
        DataSetDB.getDataSet(conn, DATASET_ID), true, false).size(),
      "Values not removed");
  }
}
