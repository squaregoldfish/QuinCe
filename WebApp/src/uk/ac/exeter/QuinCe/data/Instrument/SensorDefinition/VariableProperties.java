package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.exeter.QuinCe.data.Dataset.SensorValuesList;

/**
 * The properties for a variable.
 *
 * <p>
 * Instances of this class are built from JSON strings.
 * </p>
 *
 * @see VariablePropertiesDeserializer
 */
public class VariableProperties {

  private final List<String> coefficients;

  private Map<Long, Boolean> dependsQuestionAnswers;

  private List<PresetRunType> presetRunTypes;

  /**
   * Indicates whether the {@link Variable} requires a fixed measurement mode.
   *
   * <p>
   * By default, QuinCe will auto-detect whether an instrument is running in
   * Periodic mode or Continuous mode. Some {@link Variable}s require a certain
   * mode but take measurements in such a way that QuinCe detects it
   * incorrectly. This value allows the measurement mode to be forced.
   * </p>
   *
   * @see SensorValuesList
   */
  private int forceMeasurementMode = SensorValuesList.AUTO_DETECT_MEASUREMENT_MODE;

  protected VariableProperties() {
    this.coefficients = new ArrayList<String>();
    this.presetRunTypes = new ArrayList<PresetRunType>();
    this.dependsQuestionAnswers = new HashMap<Long, Boolean>();
  }

  protected VariableProperties(List<String> coefficients,
    Map<Long, Boolean> dependsQuestionAnswers,
    List<PresetRunType> presetRunTypes, int forceMeasurementMode) {

    this.coefficients = null != coefficients ? coefficients
      : new ArrayList<String>();

    this.dependsQuestionAnswers = null != dependsQuestionAnswers
      ? dependsQuestionAnswers
      : new HashMap<Long, Boolean>();

    this.presetRunTypes = null != presetRunTypes ? presetRunTypes
      : new ArrayList<PresetRunType>();

    this.forceMeasurementMode = forceMeasurementMode;
  }

  public List<String> getCoefficients() {
    return coefficients;
  }

  public String getRunType(long variableId) {

    String runType = null;

    PresetRunType variableRunType = presetRunTypes.stream()
      .filter(prt -> prt.getCategory().getType() == variableId).findFirst()
      .orElse(null);

    if (null != variableRunType) {
      runType = variableRunType.getDefaultRunType();
    }

    return runType;
  }

  public Map<Long, Boolean> getDependsQuestionAnswers() {
    return dependsQuestionAnswers;
  }

  public boolean hasPresetRunTypes() {
    return presetRunTypes.size() > 0;
  }

  protected List<PresetRunType> getPresetRunTypes() {
    return presetRunTypes;
  }

  /**
   * Get the forced measurement mode for the {@link Variable}.
   *
   * <p>
   * This may be {@link #AUTO_DETECT_MEASUREMENT_MODE} which indicates that
   * QuinCe should be left to auto-detect the mode.
   * </p>
   *
   * @return The forced measurement mode.
   */
  public int getForceMeasurementMode() {
    return forceMeasurementMode;
  }
}
