package uk.ac.exeter.QuinCe.web.datasets;

import java.time.LocalDateTime;

import javax.sql.DataSource;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.SensorCalibrationDB;

public class SensorCalibrationsValidCalibrationChecker
  extends DatasetValidCalibrationChecker {

  @Override
  protected void validateCalibrations(DataSource dataSource,
    Instrument instrument, LocalDateTime start, LocalDateTime end)
    throws Exception {

    // We don't do anything in this instance - any combination is valid.

    // The default implementation will have the desired effect, but we might as
    // well save some database calls.
  }

  @Override
  protected String getItemDescription() {
    return "Sensor Calibrations";
  }

  @Override
  protected CalibrationDB getCalibrationDB() {
    return SensorCalibrationDB.getInstance();
  }

}
