package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private boolean userSelectableRunType;

  protected VariableProperties() {
    this.coefficients = new ArrayList<String>();
    this.presetRunTypes = new ArrayList<PresetRunType>();
    this.dependsQuestionAnswers = new HashMap<Long, Boolean>();
    this.userSelectableRunType = true;
  }

  protected VariableProperties(List<String> coefficients,
    Map<Long, Boolean> dependsQuestionAnswers,
    List<PresetRunType> presetRunTypes, boolean userSelectableRunType) {

    this.coefficients = null != coefficients ? coefficients
      : new ArrayList<String>();

    this.dependsQuestionAnswers = null != dependsQuestionAnswers
      ? dependsQuestionAnswers
      : new HashMap<Long, Boolean>();

    this.presetRunTypes = null != presetRunTypes ? presetRunTypes
      : new ArrayList<PresetRunType>();

    this.userSelectableRunType = userSelectableRunType;
  }

  public List<String> getCoefficients() {
    return coefficients;
  }

  /**
   * Get the Run Type strings that represent a measurement for the
   * {@link Variable} with the specified ID.
   * 
   * <p>
   * In most cases there will only be one Run Type defined. Where there are
   * multiple entries, the default Run Type will be first in the list.
   * </p>
   * 
   * @param variableId
   *          The Variable's ID
   * @return The run types
   */
  public List<String> getRunTypes(long variableId) {

    List<String> runTypes = null;

    PresetRunType variableRunType = presetRunTypes.stream()
      .filter(prt -> prt.getCategory().getType() == variableId).findFirst()
      .orElse(null);

    if (null != variableRunType) {
      runTypes = variableRunType.getAllRunTypes();
    }

    return runTypes;
  }

  public Map<Long, Boolean> getDependsQuestionAnswers() {
    return dependsQuestionAnswers;
  }

  public boolean hasPresetRunTypes() {
    return presetRunTypes.size() > 0;
  }

  public List<PresetRunType> getPresetRunTypes() {
    return presetRunTypes;
  }

  public boolean userSelectableRunType() {
    return userSelectableRunType;
  }
}
