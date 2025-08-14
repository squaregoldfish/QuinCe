package uk.ac.exeter.QuinCe.data.Instrument.Calibration;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;

/**
 * Holds the uncertainty for a sensor in an instrument.
 *
 * <p>
 * Uncertainties can be defined as either an absolute value or a percentage of
 * the measured value.
 * </p>
 */
public class Uncertainty extends Calibration {

  /**
   * A fixed set of coefficient names for the Uncertainty.
   *
   * <p>
   * We repurpose the coefficient names to provide the required functionality.
   * The two names are {@code Type} (to indicate either absolute or percentage
   * uncertainty) and {@code Value} for the actual value.
   * </p>
   */
  private static LinkedHashSet<String> valueNames;

  static {
    valueNames = new LinkedHashSet<String>();
    valueNames.add("Type");
    valueNames.add("Value");
  }

  public Uncertainty(Instrument instrument, String target) {
    super(instrument, UncertaintyDB.UNCERTAINTY_CALIBRATION_TYPE, target);
  }

  public Uncertainty(Instrument instrument, long id, LocalDateTime date) {
    super(instrument, UncertaintyDB.UNCERTAINTY_CALIBRATION_TYPE, id, date);
  }

  protected Uncertainty(Uncertainty source) throws CalibrationException {
    super(source.getInstrument(), UncertaintyDB.UNCERTAINTY_CALIBRATION_TYPE,
      source.getId(), source.getDeploymentDate());
    setTarget(source.getTarget());
    setCoefficients(duplicateCoefficients(source));
  }

  public Uncertainty(long id, Instrument instrument, String target,
    LocalDateTime deploymentDate, Map<String, String> coefficients)
    throws CalibrationException {

    super(id, instrument, UncertaintyDB.UNCERTAINTY_CALIBRATION_TYPE, target);
    setDeploymentDate(deploymentDate);
    setCoefficients(coefficients);
  }

  @Override
  public LinkedHashSet<String> getCoefficientNames(boolean includeHidden) {
    return valueNames;
  }

  @Override
  public boolean coefficientsValid() {
    return true;
  }

  @Override
  public Double calibrateValue(Double rawValue) {
    return rawValue;
  }

  @Override
  public String getCoefficientsLabel() {
    return "Uncertainty";
  }

  @Override
  public Calibration makeCopy() {
    try {
      return new Uncertainty(this);
    } catch (CalibrationException e) {
      // This shouldn't happen, because it implies that we successfully created
      // in invalid object previously
      throw new RuntimeException(e);
    }
  }

  @Override
  protected boolean timeAffectsCalibration() {
    return false;
  }
}
