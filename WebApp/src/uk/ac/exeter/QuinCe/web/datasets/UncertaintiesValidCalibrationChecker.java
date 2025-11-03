package uk.ac.exeter.QuinCe.web.datasets;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.UncertaintyDB;

public class UncertaintiesValidCalibrationChecker
  extends DatasetValidCalibrationChecker {

  @Override
  protected String getItemDescription() {
    return "Uncertainties";
  }

  @Override
  protected CalibrationDB getCalibrationDB() {
    return UncertaintyDB.getInstance();
  }
}
