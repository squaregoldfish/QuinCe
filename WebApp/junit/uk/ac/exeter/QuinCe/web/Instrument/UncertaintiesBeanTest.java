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
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Uncertainty;

/**
 * Tests of calibration edits for the {@link UncertaintiesBean}.
 *
 * See
 * {@code WebApp/junit/resources/sql/web/Instrument/CalibrationBeanTest/initial_setup.html}
 * for the initial setup ({@code TARGET_1} and {@code TARGET_1} are specified in
 * this test file).
 */
@TestInstance(Lifecycle.PER_CLASS)
public class UncertaintiesBeanTest extends BaseTest {

  private static final long USER_ID = 1L;

  private static final long INSTRUMENT_ID = 3L;

  private static final String TARGET_1 = "10000";

  private static final String TARGET_2 = "10001";

  private static final String REPLACEMENT_VALUE = "7.5";

  private static final long EDITED_CALIBRATION_ID = 21L;

  private UncertaintiesBean init() throws Exception {
    initResourceManager();
    loginUser(USER_ID);
    UncertaintiesBean bean = new UncertaintiesBean();
    bean.setInstrumentId(INSTRUMENT_ID);
    bean.start();
    return bean;
  }

  protected static Map<String, String> makeCoefficients(String value) {
    Map<String, String> result = new HashMap<String, String>();
    result.put("Type", Uncertainty.TYPE_ABSOLUTE);
    result.put("Value", "1");
    return result;
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/uncertaintiesEdit" })
  @Test
  public void addClashTest() throws Exception {
    UncertaintiesBean bean = init();
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
    "resources/sql/testbase/instrument",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/uncertaintiesEdit" })
  @Test
  public void editClashTimeTest() throws Exception {
    UncertaintiesBean bean = init();
    bean.setSelectedCalibrationId(EDITED_CALIBRATION_ID);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 7, 1, 0, 0, 0));

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/uncertaintiesEdit" })
  @Test
  public void editClashTargetTest() throws Exception {
    UncertaintiesBean bean = init();
    bean.setSelectedCalibrationId(EDITED_CALIBRATION_ID);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration().setTarget(TARGET_2);

    bean.saveCalibration();
    assertFalse(bean.editedCalibrationValid());
  }

  @FlywayTest(locationsForMigrate = { "resources/sql/testbase/user",
    "resources/sql/testbase/instrument",
    "resources/sql/web/Instrument/CalibrationBeanTest/base",
    "resources/sql/web/Instrument/CalibrationBeanTest/uncertaintiesEdit" })
  @Test
  public void editClashTimeAndTargetTest() throws Exception {
    UncertaintiesBean bean = init();
    bean.setSelectedCalibrationId(EDITED_CALIBRATION_ID);
    bean.loadSelectedCalibration();
    bean.setAction(CalibrationEdit.EDIT);
    bean.getEditedCalibration()
      .setDeploymentDate(LocalDateTime.of(2023, 5, 1, 0, 0, 0));
    bean.getEditedCalibration().setTarget(TARGET_2);
  }
}
