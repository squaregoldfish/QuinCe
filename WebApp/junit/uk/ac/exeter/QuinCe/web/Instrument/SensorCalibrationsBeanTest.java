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
 * Tests of calibration edits for the {@link SensorCalibrationsBean}.
 *
 * See
 * {@code WebApp/junit/resources/sql/web/Instrument/CalibrationBeanTest/initial_setup.html}
 * for the initial setup ({@code TARGET_1} and {@code TARGET_1} are specified in
 * this test file).
 */
@TestInstance(Lifecycle.PER_CLASS)
public class SensorCalibrationsBeanTest extends BaseTest {

  private static final long USER_ID = 1L;

  private static final long INSTRUMENT_ID = 1L;

  private static final String TARGET_1 = "1";

  private static final String TARGET_2 = "3";

  private static final String REPLACEMENT_VALUE = "1.1";

  private SensorCalibrationsBean init() throws Exception {
    initResourceManager();
    loginUser(USER_ID);
    SensorCalibrationsBean bean = new SensorCalibrationsBean();
    bean.setInstrumentId(INSTRUMENT_ID);
    bean.start();
    return bean;
  }

  protected static Map<String, String> makeCoefficients(String value) {
    Map<String, String> result = new HashMap<String, String>();
    result.put("x⁵", "0");
    result.put("x⁴", "0");
    result.put("x³", "0");
    result.put("x²", "0");
    result.put("x", value);
    result.put("Intercept", "0");
    return result;
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/sensorCalibrationsEdit" })
  @Test
  public void addClashTest() throws Exception {
    SensorCalibrationsBean bean = init();
    bean.setAction(CalibrationEdit.ADD);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 2, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_1);
    bean.getEditedCalibration()
      .setCoefficients(makeCoefficients(REPLACEMENT_VALUE));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/sensorCalibrationsEdit" })
  @Test
  public void editClashTimeTest() throws Exception {
    SensorCalibrationsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 7, 1, 0, 0, 0));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/sensorCalibrationsEdit" })
  @Test
  public void editClashTargetTest() throws Exception {
    SensorCalibrationsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration().setTarget(TARGET_2);

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument", "resources/sql/testbase/variable",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/sensorCalibrationsEdit" })
  @Test
  public void editClashTimeAndTargetTest() throws Exception {
    SensorCalibrationsBean bean = init();
    bean.setSelectedCalibrationId(1L);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 5, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_2);
  }
}
