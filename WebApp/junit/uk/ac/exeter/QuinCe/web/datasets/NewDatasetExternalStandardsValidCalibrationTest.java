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
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.DefaultExternalStandard;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.ExternalStandardDB;

@TestInstance(Lifecycle.PER_CLASS)
public class NewDatasetExternalStandardsValidCalibrationTest
  extends NewDatasetValidCalibrationTest {

  @Override
  protected Stream<Arguments> getTestParams() {
    return Stream.of(Arguments.of(PRIORS_NONE, true, false),
      Arguments.of(PRIORS_NONE, false, false),
      Arguments.of(PRIORS_PARTIAL, true, false),
      Arguments.of(PRIORS_PARTIAL, false, false),
      Arguments.of(PRIORS_ALL, true, true),
      Arguments.of(PRIORS_ALL, false, true));
  }

  protected CalibrationDB getCalibrationDB() {
    return ExternalStandardDB.getInstance();
  }

  protected String[] getTargets() {
    return new String[] { "std1", "std3" };
  }

  @Override
  protected Calibration buildCalibration(String target, LocalDateTime time)
    throws Exception {

    Map<String, String> coefficients = new HashMap<String, String>();
    coefficients.put("xH₂O (with standards)", "0");
    coefficients.put("xCO₂ (with standards)", "100");

    return new DefaultExternalStandard(System.currentTimeMillis() * -1,
      getInstrument(), target, time, coefficients);
  }

  @Override
  protected boolean prepopulateExternalStandards() {
    return false;
  }
}
