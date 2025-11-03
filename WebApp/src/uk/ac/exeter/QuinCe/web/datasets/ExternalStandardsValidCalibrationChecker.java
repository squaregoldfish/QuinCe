package uk.ac.exeter.QuinCe.web.datasets;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.ExternalStandardDB;

public class ExternalStandardsValidCalibrationChecker
  extends DatasetValidCalibrationChecker {

  @Override
  protected String getItemDescription() {
    return "External Standards";
  }

  @Override
  protected CalibrationDB getCalibrationDB() {
    return ExternalStandardDB.getInstance();
  }
}
