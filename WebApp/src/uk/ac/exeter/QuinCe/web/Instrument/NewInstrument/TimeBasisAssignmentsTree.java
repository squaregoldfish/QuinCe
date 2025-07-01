package uk.ac.exeter.QuinCe.web.Instrument.NewInstrument;

import java.util.List;

import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignments;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorConfigurationException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;

/**
 * {@link SensorAssignments} tree for instruments with a Time basis.
 */
public class TimeBasisAssignmentsTree extends AssignmentsTree {

  /**
   * Indicates whether or not position information needs to be assigned for the
   * {@link uk.ac.exeter.QuinCe.data.Instrument.Instrument}.
   *
   * <p>
   * Note that this indicates whether positions are needed <i>at all</i>, and
   * not whether required position details have been assigned.
   * </p>
   */
  private final boolean needsPosition;

  protected TimeBasisAssignmentsTree(List<Variable> variables,
    SensorAssignments assignments, boolean needsPosition)
    throws SensorConfigurationException, SensorTypeNotFoundException {

    super(variables, assignments);
    this.needsPosition = needsPosition;
  }

  @Override
  protected void buildTree()
    throws SensorConfigurationException, SensorTypeNotFoundException {

    buildDateTimeNode(root, true);
    if (needsPosition) {
      buildPositionNode(root);
    }

    buildFieldNodes();
  }
}
