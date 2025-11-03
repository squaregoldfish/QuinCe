package uk.ac.exeter.QuinCe.web.datasets;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficientDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;

public class CalculationCoefficientsValidCalibrationChecker
  extends DatasetValidCalibrationChecker {

  @Override
  protected String getItemDescription() {
    return "Calibration Coefficients";
  }

  @Override
  protected CalibrationDB getCalibrationDB() {
    return CalculationCoefficientDB.getInstance();
  }
}
