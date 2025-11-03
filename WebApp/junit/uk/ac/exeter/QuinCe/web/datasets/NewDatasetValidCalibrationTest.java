package uk.ac.exeter.QuinCe.web.datasets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.InstrumentDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Calibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.DefaultExternalStandard;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.ExternalStandardDB;
import uk.ac.exeter.QuinCe.web.Instrument.CalibrationEdit;

public abstract class NewDatasetValidCalibrationTest extends BaseTest {

  protected static final int PRIORS_NONE = 0;

  protected static final int PRIORS_PARTIAL = 1;

  protected static final int PRIORS_ALL = 2;

  private static final LocalDateTime PRE_POPULATE_EXTERNAL_STANDARD_TIME = LocalDateTime
    .of(2024, 12, 31, 0, 0, 0);

  private static final LocalDateTime PRIOR_TIME = LocalDateTime.of(2025, 1, 1,
    0, 0, 0);

  private static final LocalDateTime IN_CALIBRATION_TIME = LocalDateTime
    .of(2025, 1, 15, 0, 0, 0);

  @BeforeEach
  public void init() throws Exception {
    initResourceManager();
    loginUser(1L);
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable" })
  @ParameterizedTest
  @MethodSource("getTestParams")
  public void newDatasetTest(int priors, boolean calibrationInDataset,
    boolean valid) throws Exception {

    if (prepopulateExternalStandards()) {
      populateExternalStandards();
    }

    // Create the required priors
    createPriors(priors);

    // Create the calibration in the dataset, if required
    if (calibrationInDataset) {
      createCalibration(getTargets()[0], IN_CALIBRATION_TIME);
    }

    // Initialise bean, create dataset and validate
    DataSetsBean bean = new DataSetsBean();
    bean.setCurrentInstrumentId(getInstrumentId());
    bean.startNewDataset();

    bean.getNewDataSet().setStart(LocalDateTime.of(2025, 1, 10, 0, 0, 0));
    bean.getNewDataSet().setEnd(LocalDateTime.of(2025, 1, 20, 0, 0, 0));

    bean.checkValidCalibration();

    assertEquals(valid, bean.isValidCalibration());
  }

  private void createPriors(int priorsRequired) throws Exception {

    String[] targets = getTargets();

    if (priorsRequired == PRIORS_PARTIAL) {
      createCalibration(targets[0], PRIOR_TIME);
    } else if (priorsRequired == PRIORS_ALL) {
      for (String target : targets) {
        createCalibration(target, PRIOR_TIME);
      }
    }
  }

  private void createCalibration(String target, LocalDateTime time)
    throws Exception {

    Calibration calibration = buildCalibration(target, time);

    CalibrationEdit newCalibration = new CalibrationEdit(CalibrationEdit.ADD,
      calibration);

    getCalibrationDB().commitEdits(getDataSource(),
      Arrays.asList(newCalibration));

  }

  protected Instrument getInstrument() throws Exception {
    return InstrumentDB.getInstrument(getDataSource(), getInstrumentId());
  }

  /**
   * Create a {@link Calibration} object.
   *
   * @param target
   *          The calibration target.
   * @param time
   *          The calibration time.
   * @return The Calibration object.
   */
  protected abstract Calibration buildCalibration(String target,
    LocalDateTime time) throws Exception;

  /**
   * Get the test arguments for the specific calibration type.
   *
   * @return The test arguments.
   */
  protected abstract Stream<Arguments> getTestParams();

  /**
   * Get the targets for the calibration type.
   */
  protected abstract String[] getTargets();

  /**
   * Get the {@link CalibrationDB} instance for the calibration type.
   *
   * @return The {@link CalibrationDB} instance.
   */
  protected abstract CalibrationDB getCalibrationDB();

  /**
   * Indicates whether external standards should be populated before the test
   * runs.
   *
   * <p>
   * Some tests require that the external standards are created regardless of
   * the test context, to ensure that the external standards check doesn't fail
   * even when we aren't interested in it. Setting the return value of this
   * method to {@code true} ensures that external standards are populated for
   * every test.
   * </p>
   *
   * @return {@code true} if the external standards should be populated;
   *         {@code false} if not.
   */
  protected abstract boolean prepopulateExternalStandards();

  /**
   * Get the database ID of the test {@link Instrument} we are working with.
   *
   * @return The instrument's ID.
   */
  protected long getInstrumentId() {
    return 1L;
  }

  private void populateExternalStandards() throws Exception {
    Map<String, String> coefficients = new HashMap<String, String>();
    coefficients.put("xH₂O (with standards)", "0");
    coefficients.put("xCO₂ (with standards)", "100");

    Calibration std1Calib = new DefaultExternalStandard(
      System.currentTimeMillis() * -1, getInstrument(), "std1",
      PRE_POPULATE_EXTERNAL_STANDARD_TIME, coefficients);
    CalibrationEdit std1Edit = new CalibrationEdit(CalibrationEdit.ADD,
      std1Calib);

    Calibration std3Calib = new DefaultExternalStandard(
      System.currentTimeMillis() * -1, getInstrument(), "std3",
      PRE_POPULATE_EXTERNAL_STANDARD_TIME, coefficients);
    CalibrationEdit std3Edit = new CalibrationEdit(CalibrationEdit.ADD,
      std3Calib);

    ExternalStandardDB.getInstance().commitEdits(getDataSource(),
      Arrays.asList(std1Edit, std3Edit));
  }
}
