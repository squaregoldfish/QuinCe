package uk.ac.exeter.QuinCe.data.Instrument.Calibration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.utils.ParameterException;
import uk.ac.exeter.QuinCe.utils.StringUtils;

/**
 * Represents a external standard calibration
 *
 * @author Steve Jones
 *
 */
public class ExternalStandard extends Calibration {

  private static final String XH2O_NAME = "xH₂O (with standards)";

  /**
   * Contains the label for the concentration value (constructed in the
   * {@code static} block)
   */
  private List<String> valueNames;

  /**
   * Create an empty external standard placeholder that isn't bound to a
   * particular standard
   *
   * @param instrumentId
   *          The instrument ID
   */
  public ExternalStandard(Instrument instrument) {
    super(instrument, ExternalStandardDB.EXTERNAL_STANDARD_CALIBRATION_TYPE);
    buildValueNames();
  }

  /**
   * Creates an empty external standard for a specified standard
   *
   * @param instrumentid
   *          The instrument ID
   * @param standard
   *          The standard
   */
  protected ExternalStandard(Instrument instrument, String standard) {
    super(instrument, ExternalStandardDB.EXTERNAL_STANDARD_CALIBRATION_TYPE,
      standard);
    buildValueNames();
  }

  /**
   * Construct a complete external standard object with all data
   *
   * @param instrumentId
   *          The instrument ID
   * @param target
   *          The target external standard
   * @param deploymentDate
   *          The deployment date
   * @param coefficients
   *          The standard concentration
   * @throws ParameterException
   *           If the calibration details are invalid
   */
  protected ExternalStandard(long id, Instrument instrument, String target,
    LocalDateTime deploymentDate, List<String> coefficients)
    throws ParameterException {
    super(id, instrument, ExternalStandardDB.EXTERNAL_STANDARD_CALIBRATION_TYPE,
      target);
    buildValueNames();

    if (null != target) {
      setDeploymentDate(deploymentDate);
      setCoefficients(coefficients);
      if (!validate()) {
        throw new ParameterException("Deployment date/coefficients",
          "Calibration deployment is invalid");
      }
    }
  }

  private void buildValueNames() {
    // The value names are all the sensor type names that have internal
    // calibrations
    valueNames = instrument.getSensorAssignments().keySet().stream()
      .filter(st -> st.hasInternalCalibration()).map(st -> st.getShortName())
      .sorted().collect(Collectors.toList());
  }

  @Override
  public List<String> getCoefficientNames() {
    if (null == valueNames) {
      buildValueNames();
    }

    return valueNames;
  }

  @Override
  public String buildHumanReadableCoefficients() {
    List<String> resultEntries = new ArrayList<String>(valueNames.size());

    for (int i = 0; i < valueNames.size(); i++) {
      if (!valueNames.get(i).equals(XH2O_NAME)) {
        resultEntries
          .add(valueNames.get(i) + " = " + getConcentration(valueNames.get(i)));
      }
    }

    String result;

    if (resultEntries.size() == 1) {
      result = resultEntries.get(0).split("=")[1].trim();
    } else {
      result = StringUtils.collectionToDelimited(resultEntries, ";");
    }

    return result;
  }

  /**
   * Get the concentration of the external standard
   *
   * @return The concentration
   */
  public double getConcentration(String value) throws CalibrationException {

    if (null == coefficients) {
      initialiseCoefficients();
    }

    int valueIndex = getValueIndex(value);
    if (valueIndex < 0) {
      throw new CalibrationException("Unknown value name " + value);
    }

    return coefficients.get(valueIndex).getDoubleValue();
  }

  /**
   * Set the concentration of the external standard
   *
   * @param concentration
   *          The concentration
   */
  public void setConcentration(String value, String concentration)
    throws CalibrationException {

    if (null == coefficients) {
      initialiseCoefficients();
    }

    if (value.equals(XH2O_NAME)) {
      throw new CalibrationException("Cannot set concentration for xH2O");
    }

    int valueIndex = getValueIndex(value);
    if (valueIndex < 0) {
      throw new CalibrationException("Unknown value name " + value);
    }

    coefficients.set(valueIndex,
      new CalibrationCoefficient(value, concentration));
  }

  @Override
  public boolean coefficientsValid() {
    boolean result = true;

    if (null != coefficients) {
      if (coefficients.size() != valueNames.size()) {
        result = false;
      } else {
        // All concentrations must be >= 0
        if (getCoefficients().stream().anyMatch(c -> c.getDoubleValue() < 0)) {
          result = false;
        }

        // xH2O must be zero
        if (getCoefficients().get(getXH2OIndex()).getDoubleValue() != 0.0) {
          result = false;
        }
      }
    }

    return result;
  }

  @Override
  public Double calibrateValue(Double rawValue) {
    return rawValue;
  }

  @Override
  public List<CalibrationCoefficient> getEditableCoefficients() {
    // We never use xH2O - it's fixed at zero in the code
    // See setConcentration
    List<CalibrationCoefficient> result = new ArrayList<CalibrationCoefficient>(
      getCoefficients());

    if (getXH2OIndex() > -1) {
      result.remove(getXH2OIndex());
    }

    return result;
  }

  private int getXH2OIndex() {
    return getValueIndex(XH2O_NAME);
  }

  private int getValueIndex(String valueName) {
    return valueNames.indexOf(valueName);
  }

  public boolean allZero() {
    return getCoefficients().stream().allMatch(c -> c.getDoubleValue() == 0D);
  }
}
