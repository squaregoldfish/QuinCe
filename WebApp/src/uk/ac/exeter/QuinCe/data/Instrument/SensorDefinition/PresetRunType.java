package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.ac.exeter.QuinCe.data.Instrument.RunTypes.RunTypeCategory;

public class PresetRunType implements Comparable<PresetRunType> {

  private List<String> runTypes;
  private RunTypeCategory category;

  protected PresetRunType(Collection<String> runTypes,
    RunTypeCategory category) {
    this.runTypes = new ArrayList<String>();
    this.runTypes.addAll(runTypes);
    this.category = category;
  }

  public boolean containsRunType(String runType) {
    return runTypes.contains(runType.toLowerCase());
  }

  public RunTypeCategory getCategory() {
    return category;
  }

  public String getDefaultRunType() {
    return runTypes.get(0);
  }

  public List<String> getAllRunTypes() {
    return runTypes;
  }

  @Override
  public String toString() {
    return runTypes.toString() + " -> " + category.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(runTypes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PresetRunType other = (PresetRunType) obj;

    // We assume that the first run type is sufficient to distinguish.
    return runTypes.get(0).equals(other.runTypes.get(0));
  }

  @Override
  public int compareTo(PresetRunType o) {
    // We assume that the first run type is sufficient to distinguish.
    return runTypes.get(0).compareTo(o.runTypes.get(0));
  }

  /**
   * Get the Run Type Category for a specified Run Type from a collection of
   * {@code PresetRunType}s.
   * 
   * <p>
   * Returns {@code null} if the Run Type is not present.
   * </p>
   * 
   * @param presetRunTypes
   *          The preset run types.
   * @param runType
   *          The Run Type.
   * @return The Run Type Category.
   */
  public static RunTypeCategory getRunTypeCategory(
    Collection<PresetRunType> presetRunTypes, String runType) {

    RunTypeCategory result = null;

    for (PresetRunType presetRunType : presetRunTypes) {
      if (presetRunType.getAllRunTypes().contains(runType.toLowerCase())) {
        result = presetRunType.getCategory();
        break;
      }
    }

    return result;
  }
}
