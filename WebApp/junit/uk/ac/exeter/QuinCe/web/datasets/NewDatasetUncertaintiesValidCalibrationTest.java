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
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Uncertainty;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.UncertaintyDB;

@TestInstance(Lifecycle.PER_CLASS)
public class NewDatasetUncertaintiesValidCalibrationTest
  extends NewDatasetValidCalibrationTest {

  @Override
  protected Stream<Arguments> getTestParams() {
    return Stream.of(Arguments.of(PRIORS_NONE, true, false),
      Arguments.of(PRIORS_NONE, false, true),
      Arguments.of(PRIORS_PARTIAL, true, false),
      Arguments.of(PRIORS_PARTIAL, false, false),
      Arguments.of(PRIORS_ALL, true, false),
      Arguments.of(PRIORS_ALL, false, true));
  }

  protected CalibrationDB getCalibrationDB() {
    return UncertaintyDB.getInstance();
  }

  protected String[] getTargets() {
    return new String[] { "1", "2", "3", "4", "5", "6" };
  }

  @Override
  protected Calibration buildCalibration(String target, LocalDateTime time)
    throws Exception {

    Map<String, String> coefficients = new HashMap<String, String>();
    coefficients.put("Type", "1");
    coefficients.put("Value", "0.2");

    return new Uncertainty(System.currentTimeMillis() * -1, getInstrument(),
      target, time, coefficients);
  }

  @Override
  protected boolean prepopulateExternalStandards() {
    return true;
  }
}
