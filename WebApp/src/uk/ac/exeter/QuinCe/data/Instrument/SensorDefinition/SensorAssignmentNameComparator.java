package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

import java.util.Comparator;

import uk.ac.exeter.QuinCe.utils.NaturalOrderComparator;

/**
 * Comparator for sorting {@link SensorAssignment} objects by their display
 * name.
 *
 * <p>
 * The comparison includes the name of the source data file so that sensors from
 * the same file are grouped together.
 * </p>
 */
public class SensorAssignmentNameComparator
  implements Comparator<SensorAssignment> {

  @Override
  public int compare(SensorAssignment arg0, SensorAssignment arg1) {

    int result = NaturalOrderComparator.getInstance()
      .compare(arg0.getDataFile(), arg1.getDataFile());

    if (result == 0) {
      result = NaturalOrderComparator.getInstance()
        .compare(arg0.getSensorName(), arg1.getSensorName());
    }

    return result;
  }
}
