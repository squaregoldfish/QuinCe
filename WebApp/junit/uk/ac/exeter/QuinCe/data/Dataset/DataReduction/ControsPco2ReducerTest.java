package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.DataSetDB;
import uk.ac.exeter.QuinCe.data.Dataset.DataSetDataDB;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetMeasurements;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.TimeDataSet;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.InstrumentDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficientDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;
import uk.ac.exeter.QuinCe.web.Instrument.NewInstrument.DateTimeFormatsBean;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Tests for the 4H-Jena CONTROS sensor's data reduction.
 *
 * <p>
 * Pre- and post-calibrations are set for {@code 2021-06-21T00:00:00Z} and
 * {@code 2021-07-11T00:00:00Z} respectively as Flyway database entries.
 * </p>
 *
 * <p>
 * Migrations are also prepared for datasets with different sets of
 * measurements:
 * </p>
 * <ul>
 * <li>Zero runs before and after measurements.</li>
 * <li>Zero run before measurements only.</li>
 * <li>Zero run after measurements only.</li>
 * </ul>
 *
 * <p>
 * The following test combinations are covered:
 * </p>
 * <table>
 * <caption>Test combinations for CONTROS data reduction.</caption>
 * <tr>
 * <th>Variable Mode</th>
 * <th>Has pre-calibration?</th>
 * <th>Has post-calibration?</th>
 * <th>Data has pre-zero?</th>
 * <th>Data has post-zero?</th>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Continuous</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Zero after sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * </tr>
 * <tr>
 * <td>Zero before sleep</td>
 * <td style="text-align: center">Y</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">N</td>
 * <td style="text-align: center">Y</td>
 * </tr>
 * </table>
 *
 * <p>
 * Tests for the situation where pre-calibrations are not present are not
 * conducted, because datasets cannot be created in such a situation. Similarly,
 * the behaviour of the data reduction where there are no Zeroing values is
 * undefined, so there are no tests for this case.
 * </p>
 */
public class ControsPco2ReducerTest extends DataReducerTest {

  /**
   * The database ID of the {@link Instrument} in the FlyWay test data.
   */
  private static final long INSTRUMENT_ID = 124L;

  /**
   * The database ID of the {@link DataSet} in the FlyWay test data.
   */
  private static final long DATASET_ID = 2765L;

  /**
   * Date of the {@link Measurement} whose data reduction will be tested.
   */
  private static final LocalDateTime MEASUREMENT_TIME = LocalDateTime
    .parse("2023-06-08T23:50:13.000Z", DateTimeFormatsBean.DT_ISO_MS_F);

  /**
   * Initialise the Resource Manager.
   */
  @BeforeEach
  public void setup() {
    initResourceManager();
  }

  /**
   * Destroy the Resource Manager.
   */
  @AfterEach
  public void tearDown() {
    ResourceManager.destroy();
  }

  /**
   * Create the data reducer for testing.
   *
   * @return The data reducer.
   * @throws Exception
   *           If the reducer cannot be created.
   */
  private ControsPco2Reducer makeReducer(TimeDataSet dataset) throws Exception {
    CalibrationSet calculationCoefficients = CalculationCoefficientDB
      .getInstance().getCalibrationSet(getConnection(), dataset);
    return new ControsPco2Reducer(getVariable(), dataset.getAllProperties(),
      calculationCoefficients);
  }

  /**
   * Get the CONTROS {@link Variable}.
   *
   * @return The Variable.
   * @throws Exception
   *           If the Variable cannot be retrieved.
   */
  private Variable getVariable() throws Exception {
    List<Variable> variables = InstrumentDB.getAllVariables(getDataSource());
    return variables.stream().filter(v -> v.getName().equals("CONTROS pCO₂"))
      .findAny().get();
  }

  /**
   * Get the configured {@link Instrument} for the testing {@link DataSet}ß.
   *
   * @return The Instrument.
   * @throws Exception
   *           If the Instrument cannot be retrieved.
   */
  private Instrument getInstrument() throws Exception {
    return InstrumentDB.getInstrument(getConnection(), INSTRUMENT_ID);
  }

  /**
   * Get the test {@link DataSet} from the database, overriding the measurement
   * mode as specified.
   *
   * @param variableMode
   *          The measurement mode.
   * @return The DataSet.
   * @throws Exception
   *           If the DataSet cannot be retrieved.
   */
  private TimeDataSet getDataset(String variableMode) throws Exception {

    TimeDataSet dataSet = (TimeDataSet) DataSetDB.getDataSet(getConnection(),
      DATASET_ID);

    Properties varProps = dataSet.getAllProperties()
      .get(getVariable().getName());
    varProps.setProperty("zero_mode", variableMode);

    return dataSet;
  }

  /**
   * Get the {@link Measurement}s for the test {@link DataSet}.
   *
   * @param instrument
   *          The parent {@link Instrument}.
   * @return The measurements.
   * @throws Exception
   *           If the measurements cannot be retrieved.
   */
  private DatasetMeasurements getMeasurements(DataSet dataset)
    throws Exception {
    return DataSetDataDB.getMeasurementsByRunType(getConnection(), dataset);
  }

  /**
   * Get the {@link Measurement} whose data reduction is to be tested.
   *
   * @param allMeasurements
   *          The test {@link DataSet}'s measurements.
   * @return The {@link Measurement} to be tested.
   */
  private Measurement getTestMeasurement(DatasetMeasurements allMeasurements) {
    return allMeasurements.getOrderedMeasurements().stream()
      .filter(m -> m.getCoordinate().getTime().equals(MEASUREMENT_TIME))
      .findAny().get();
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre- and post-zeros are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void continuousCalPrePostZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8077D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4474.6864D, 0.5671F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(379.5873D, 0.1374F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(406.2867D, 0.1518F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(404.8174D, 0.1573F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void continuousCalPrePostZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8081D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.808D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(382.4052D, 0.1386F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(409.3028D, 0.153F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(407.8226D, 0.1586F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void continuousCalPrePostZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8073D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.2229D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.2513D, 0.1365F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(402.7161D, 0.1508F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.2597D, 0.1563F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre- and Post-zero are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void continuousCalPreZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8077D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4474.6864D, 0.5671F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(380.2868D, 0.1377F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(407.0354D, 0.1521F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(405.5634D, 0.1576F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void continuousCalPreZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8081D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(383.1211D, 0.1388F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(410.069D, 0.1533F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(408.586D, 0.159F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Continuous mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void continuousCalPreZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Continuous");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8072D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.2229D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.9312D, 0.1368F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(403.4438D, 0.151F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.9848D, 0.1566F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre- and post-zeros are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroBeforeSleepCalPrePostZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8073D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.2229D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.2513D, 0.1365F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(402.7161D, 0.1508F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.2597D, 0.1563F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroBeforeSleepCalPrePostZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(Double.NaN, 0F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroBeforeSleepCalPrePostZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8073D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.2229D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.2513D, 0.1365F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(402.7161D, 0.1508F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.2597D, 0.1563F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre- and Post-zero are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroBeforeSleepCalPreZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8073D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.223D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.9312D, 0.1368F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(403.4438D, 0.151F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.9848D, 0.1566F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroBeforeSleepCalPreZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(Double.NaN, 0F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero Before Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroBeforeSleepCalPreZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero before sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8073D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4445.223D, 0.5776F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(376.9312D, 0.1368F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(403.4438D, 0.151F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(401.9848D, 0.1566F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre- and post-zeros are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroAfterSleepCalPrePostZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.808D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(382.4052D, 0.1386F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(409.3028D, 0.153F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(407.8226D, 0.1586F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroAfterSleepCalPrePostZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.808D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(382.4052D, 0.1386F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(409.3028D, 0.153F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(407.8226D, 0.1586F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre- and post calibrations are present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/post-calibration" })
  @Test
  public void zeroAfterSleepCalPrePostZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(Double.NaN, 0F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre- and Post-zero are present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_both_zeros",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroAfterSleepCalPreZeroPrePost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.8081D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(383.1211D, 0.1388F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(410.069D, 0.1533F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(408.586D, 0.159F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Pre-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_pre_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroAfterSleepCalPreZeroPre() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(0.808D, 0.000002F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(0.7487D, 0.000007F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(4499.4805D, 0.577F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(383.1211D, 0.1388F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(410.0691D, 0.1533F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(408.5861D, 0.159F);
  }

  /**
   * Test for the reducer with the following setup:
   *
   * <ul>
   * <li>Zero After Sleep mode.</li>
   * <li>Pre-calibration is present.</li>
   * <li>Post-zero is present.</li>
   * </ul>
   *
   * @throws Exception
   *           If any error occurs.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/dataset_post_zero",
    "resources/sql/data/DataSet/DataReduction/ControsPco2ReducerTest/pre-calibration" })
  @Test
  public void zeroAfterSleepCalPreZeroPost() throws Exception {

    Instrument instrument = getInstrument();
    TimeDataSet dataset = getDataset("Zero after sleep");
    ControsPco2Reducer reducer = makeReducer(dataset);
    DatasetMeasurements measurements = getMeasurements(dataset);
    DatasetSensorValues allSensorValues = DataSetDataDB
      .getSensorValues(getConnection(), dataset, true, false);

    reducer.preprocess(getConnection(), instrument, dataset,
      measurements.getOrderedMeasurements());

    DataReductionRecord dataReductionRecord = reducer.performDataReduction(
      instrument, getTestMeasurement(measurements), allSensorValues,
      getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Zero S₂beam"),
        "Zero S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("S₂beam"), "S₂beam")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("Sproc"), "Sproc")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("xCO₂"), "xCO₂")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("pCO₂ SST"),
        "pCO₂ SST")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(dataReductionRecord.getCalculationValue("fCO₂"), "fCO₂")
      .matches(Double.NaN, 0F);
  }
}
