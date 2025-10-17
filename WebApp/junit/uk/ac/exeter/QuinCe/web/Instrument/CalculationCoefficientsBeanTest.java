package uk.ac.exeter.QuinCe.web.Instrument;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

/**
 * Tests of calibration edits for the {@link CalculationCoefficientsBean}.
 *
 * <p>
 * See
 * {@code WebApp/junit/resources/sql/web/Instrument/CalibrationBeanTest/initial_setup.html}
 * for the initial setup ({@code TARGET_1} and {@code TARGET_1} are specified in
 * this test file as fixed fields {@link #TARGET_1} and {@link #TARGET_2}). This
 * will use the CONTROS pCO2 system as the testing basis. Most coefficients will
 * be set with prior and post values for the full period, and we will adjust the
 * coefficients for two parameters ({@code F} and {@code Runtime}) to perform
 * the tests. The {@code k1}, {@code k2} and {@code k3} coefficients will remain
 * constant.
 * </p>
 *
 */
@TestInstance(Lifecycle.PER_CLASS)
public class CalculationCoefficientsBeanTest extends BaseTest {
  /**
   * Dummy User ID.
   */
  private static final long USER_ID = 1L;

  /**
   * Dummy Instrument ID.
   */
  private static final long INSTRUMENT_ID = 1L;

  /**
   * Name of the first target in a pair of created calibrations.
   */
  private static final String TARGET_1 = "6.F";

  /**
   * Name of the second target in a pair of created calibrations.
   */
  private static final String TARGET_2 = "6.Runtime";

  /**
   * Value to use when editing the value of an existing coefficient.
   */
  private static final String REPLACEMENT_VALUE = "1000";

  /**
   * Initialise the bean.
   *
   * @return The bean.
   * @throws Exception
   *           If the bean cannot be initialised.
   */
  private CalculationCoefficientsBean init() throws Exception {
    initResourceManager();
    loginUser(USER_ID);
    CalculationCoefficientsBean bean = new CalculationCoefficientsBean();
    bean.setInstrumentId(INSTRUMENT_ID);
    bean.start();
    return bean;
  }

  /**
   * Generate a calibration coefficients map containing the specified value.
   *
   * <p>
   * `CalculationCoefficient`s only contain one value, which is called `Value`
   * in the map.
   * </p>
   *
   * @param value
   *          The coefficient value.
   * @return The coefficients map.
   */
  protected static Map<String, String> makeCoefficients(String value) {
    Map<String, String> result = new HashMap<String, String>();
    result.put("Value", value);
    return result;
  }

  /**
   * Test adding a calibration that clashes with an existing calibration.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void addClashTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setAction(CalibrationEdit.ADD);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 2, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_1);
    bean.getEditedCalibration()
      .setCoefficients(makeCoefficients(REPLACEMENT_VALUE));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  /**
   * Test editing a calibration to clash with another calibration's time.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void editClashTimeTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 7, 1, 0, 0, 0));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  /**
   * Test editing a calibration to clash with another calibration's target.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void editClashTargetTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration().setTarget(TARGET_2);

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  /**
   * Test editing a calibration to clash with another calibration's time and
   * target.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void editClashTimeAndTargetTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 5, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_2);

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  /**
   * Test adding a new calibration in the middle of a dataset.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void addInterimTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setAction(CalibrationEdit.ADD);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 3, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_1);
    bean.getEditedCalibration()
      .setCoefficients(makeCoefficients(REPLACEMENT_VALUE));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  /**
   * Test moving a calibration to the middle of an existing dataset.
   *
   * @throws Exception
   *           If any test action throws an Exception.
   */
  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/calculationCoefficientsEdit" })
  @Test
  public void editMoveToInterimTest() throws Exception {
    CalculationCoefficientsBean bean = init();
    bean.setSelectedCalibrationId(3L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 3, 1, 0, 0, 0));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }
}
