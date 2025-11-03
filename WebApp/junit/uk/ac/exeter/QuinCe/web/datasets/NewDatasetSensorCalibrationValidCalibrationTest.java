package uk.ac.exeter.QuinCe.web.datasets;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Calibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.PolynomialSensorCalibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.SensorCalibrationDB;

@TestInstance(Lifecycle.PER_CLASS)
public class NewDatasetSensorCalibrationValidCalibrationTest
  extends NewDatasetValidCalibrationTest {

  @Override
  protected Stream<Arguments> getTestParams() {
    return Stream.of(Arguments.of(PRIORS_NONE, true, true),
      Arguments.of(PRIORS_NONE, false, true),
      Arguments.of(PRIORS_PARTIAL, true, true),
      Arguments.of(PRIORS_PARTIAL, false, true),
      Arguments.of(PRIORS_ALL, true, true),
      Arguments.of(PRIORS_ALL, false, true));
  }

  protected CalibrationDB getCalibrationDB() {
    return SensorCalibrationDB.getInstance();
  }

  protected String[] getTargets() {
    return new String[] { "1", "3" };
  }

  @Override
  protected Calibration buildCalibration(String target, LocalDateTime time)
    throws Exception {

    Map<String, String> coefficients = new HashMap<String, String>();
    coefficients.put("x⁵", "0");
    coefficients.put("x⁴", "0");
    coefficients.put("x³", "0");
    coefficients.put("x²", "0");
    coefficients.put("x", "0");
    coefficients.put("Intercept", "1020");

    return new PolynomialSensorCalibration(System.currentTimeMillis() * -1,
      getInstrument(), target, time, coefficients);
  }

  @Override
  protected boolean prepopulateExternalStandards() {
    return true;
  }
}
