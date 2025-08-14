package uk.ac.exeter.QuinCe.data.Instrument.Calibration;

import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.InstrumentException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignments;
import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;

/**
 * Implementation of the {@link CalibrationDB} class for handling
 * {@link Uncertainty}(ie)s.
 */
public class UncertaintyDB extends CalibrationDB {

  /**
   * The calibration type string stored in the database for
   * {@link Uncertainty}(ie)s.
   */
  public static final String UNCERTAINTY_CALIBRATION_TYPE = "UNCERTAINTY";

  /**
   * The singleton instance of this class.
   */
  private static UncertaintyDB instance = null;

  /**
   * Retrieve the singleton instance of this class.
   *
   * @return The singleton.
   */
  public static UncertaintyDB getInstance() {
    if (null == instance) {
      instance = new UncertaintyDB();
    }

    return instance;
  }

  @Override
  public Map<String, String> getTargets(Connection conn, Instrument instrument)
    throws DatabaseException, RecordNotFoundException, InstrumentException {

    Map<String, String> targets = new TreeMap<String, String>();

    SensorAssignments assignments = instrument.getSensorAssignments();

    // Get all the sensor names for the non-diagnostic sensor types
    assignments.keySet().stream().filter(st -> !st.isDiagnostic())
      .forEach(st -> {
        assignments.get(st).stream()
          .forEach(a -> targets.put(a.getSensorName(), a.getSensorName()));
      });

    return targets;
  }

  @Override
  public String getCalibrationType() {
    return UNCERTAINTY_CALIBRATION_TYPE;
  }

  @Override
  public boolean allowCalibrationChangeInDataset() {
    return true;
  }

  @Override
  public boolean usePostCalibrations() {
    return false;
  }

  @Override
  public boolean timeAffectesCalibration() {
    return false;
  }

  @Override
  public boolean completeSetRequired() {
    return false;
  }

}
