package uk.ac.exeter.QuinCe.web.datasets;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficient;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficientDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Calibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;

@TestInstance(Lifecycle.PER_CLASS)
public class NewDatasetCalculationCoefficientsValidCalibrationTest
  extends NewDatasetValidCalibrationTest {

  @Override
  protected Stream<Arguments> getTestParams() {
    return Stream.of(Arguments.of(PRIORS_NONE, true, false),
      Arguments.of(PRIORS_NONE, false, false),
      Arguments.of(PRIORS_PARTIAL, true, false),
      Arguments.of(PRIORS_PARTIAL, false, false),
      Arguments.of(PRIORS_ALL, true, false),
      Arguments.of(PRIORS_ALL, false, true));
  }

  protected CalibrationDB getCalibrationDB() {
    return CalculationCoefficientDB.getInstance();
  }

  protected String[] getTargets() {
    return new String[] { "6.F", "6.Response Time", "6.Runtime", "6.k1", "6.k2",
      "6.k3" };
  }

  @Override
  protected Calibration buildCalibration(String target, LocalDateTime time)
    throws Exception {

    Map<String, String> coefficients = new HashMap<String, String>();
    coefficients.put("Value", "12");

    return new CalculationCoefficient(System.currentTimeMillis() * -1,
      getInstrument(), target, time, coefficients);
  }

  @Override
  protected boolean prepopulateExternalStandards() {
    return false;
  }

  @Override
  protected long getInstrumentId() {
    return 2L;
  }
}
