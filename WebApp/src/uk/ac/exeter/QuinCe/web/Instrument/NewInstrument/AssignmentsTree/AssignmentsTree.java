package uk.ac.exeter.QuinCe.web.Instrument.NewInstrument.AssignmentsTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.TreeNode;

import uk.ac.exeter.QuinCe.data.Instrument.FileDefinition;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.DateTimeColumnAssignment;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.DateTimeSpecification;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.DateTimeSpecificationException;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.PositionSpecification;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignment;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignmentException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignments;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorConfigurationException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorsConfiguration;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.VariableAttributes;
import uk.ac.exeter.QuinCe.web.Instrument.NewInstrument.FileDefinitionBuilder;
import uk.ac.exeter.QuinCe.web.Instrument.NewInstrument.NewInstrumentFileSet;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Holds details of the assigned sensors in tree form for the
 * {@code assign_variables.xhtml} page.
 *
 * <p>
 * Concrete implementations will configure the tree for different
 * {@code Instrument} bases.
 * </p>
 */
public abstract class AssignmentsTree {

  /**
   * Node type indicating that the date/time has not been fully assigned.
   */
  protected static final String DATETIME_UNFINISHED = "UNFINISHED_DATETIME";

  /**
   * Node type indicating that the date/time has been fully assigned.
   */
  protected static final String DATETIME_FINISHED = "FINISHED_DATETIME";

  /**
   * Node type indicating an unassigned date/time node.
   */
  private static final String DATETIME_UNASSIGNED = "UNASSIGNED_DATETIME";

  /**
   * Node type indicating an assigned date/time node.
   */
  private static final String DATETIME_ASSIGNED = "ASSIGNED_DATETIME";

  /**
   * Node type for an assigned date/time column.
   */
  protected static final String DATETIME_ASSIGNMENT = "DATETIME_ASSIGNMENT";

  /**
   * Node type indicating that the longitude has not been fully assigned.
   */
  private static final String LONGITUDE_UNASSIGNED = "UNASSIGNED_LONGITUDE";

  /**
   * Node type indicating that the longitude has been fully assigned.
   */
  private static final String LONGITUDE_ASSIGNED = "ASSIGNED_LONGITUDE";

  /**
   * Node type for an assigned longitude column.
   */
  private static final String LONGITUDE_ASSIGNMENT = "LONGITUDE_ASSIGNMENT";

  /**
   * Node type indicating that the latitude has not been fully assigned.
   */
  private static final String LATITUDE_UNASSIGNED = "UNASSIGNED_LATITUDE";

  /**
   * Node type indicating that the latitude has been fully assigned.
   */
  private static final String LATITUDE_ASSIGNED = "ASSIGNED_LATITUDE";

  /**
   * Node type for an assigned latitude column.
   */
  private static final String LATITUDE_ASSIGNMENT = "LATITUDE_ASSIGNMENT";

  /**
   * Node type indicating that a hemisphere entry has not been fully assigned.
   */
  private static final String HEMISPHERE_UNASSIGNED = "UNASSIGNED_HEMISPHERE";

  /**
   * Node type indicating that a hemisphere entry has been fully assigned.
   */
  private static final String HEMISPHERE_ASSIGNED = "ASSIGNED_HEMISPHERE";

  /**
   * Node type for an assigned hemisphere column.
   */
  private static final String HEMISPHERE_ASSIGNMENT = "HEMISPHERE_ASSIGNMENT";

  /**
   * Node type for a variable that has not been fully assigned (including the
   * position).
   */
  protected static final String VAR_UNFINISHED = "UNFINISHED_VARIABLE";

  /**
   * Node type for a variable that has been fully assigned (including the
   * position).
   */
  protected static final String VAR_FINISHED = "FINISHED_VARIABLE";

  /**
   * Node type indicating that a {@link SensorType} has not been fully assigned.
   */
  protected static final String SENSOR_TYPE_UNASSIGNED = "UNASSIGNED_SENSOR_TYPE";

  /**
   * Node type indicating that a {@link SensorType} has been fully assigned.
   */
  protected static final String SENSOR_TYPE_ASSIGNED = "ASSIGNED_SENSOR_TYPE";

  /**
   * Node type for an assigned {@link SensorType} column.
   */
  protected static final String ASSIGNMENT = "SENSOR_TYPE_ASSIGNMENT";

  /**
   * The set of sensor assignments being built.
   */
  protected final SensorAssignments assignments;

  /**
   * The set of {@link FileDefinition}s specified for the
   * {@link uk.ac.exeter.QuinCe.data.Instrument.Instrument}.
   */
  protected final NewInstrumentFileSet files;

  /**
   * The {@link Variable}s measured by the
   * {@link uk.ac.exeter.QuinCe.data.Instrument.Instrument}.
   */
  protected final List<Variable> variables;

  /**
   * Records the expanded/collapsed state of tree nodes.
   */
  private Map<String, Boolean> expandStates = new HashMap<String, Boolean>();

  /**
   * Initialise and construct the assignments tree.
   *
   * @param variables
   *          The {@link Variable}s measured by the
   *          {@link uk.ac.exeter.QuinCe.data.Instrument.Instrument}.
   * @param assignments
   *          The object holding the assignments columns columns to times,
   *          positions, {@link SensorType}s etc.
   * @param needsPosition
   *          Indicates whether or not position columns are needed.
   * @throws SensorConfigurationException
   *           If the system's sensor configuration is invalid.
   * @throws SensorTypeNotFoundException
   *           If any referenced {@link SensorType} does not exist.
   */
  protected AssignmentsTree(NewInstrumentFileSet files,
    List<Variable> variables, SensorAssignments assignments)
    throws SensorConfigurationException, SensorTypeNotFoundException {

    this.variables = variables;
    this.assignments = assignments;
    this.files = files;
  }

  protected void buildPositionNodes(TreeNode<AssignmentsTreeNodeData> parent) {
    AssignmentsTreeNode<AssignmentsTreeNodeData> positionNode = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
      this, null, new StringNodeData("Position"), parent);

    makePositionNodes("Longitude", positionNode);
    makePositionNodes("Latitude", positionNode);

    boolean allAssigned = true;

    for (TreeNode<AssignmentsTreeNodeData> child : positionNode.getChildren()) {
      if (child.getType().equals(LONGITUDE_UNASSIGNED)
        || child.getType().equals(LATITUDE_UNASSIGNED)
        || child.getType().equals(HEMISPHERE_UNASSIGNED)) {
        allAssigned = false;
        break;
      }
    }

    positionNode.setType(allAssigned ? VAR_FINISHED : VAR_UNFINISHED);
  }

  protected void buildSensorTypeNodes(TreeNode<AssignmentsTreeNodeData> parent)
    throws SensorConfigurationException, SensorTypeNotFoundException,
    SensorAssignmentException {
    SensorsConfiguration sensorConfig = ResourceManager.getInstance()
      .getSensorsConfiguration();

    HashMap<Long, VariableAttributes> varAttributes = new HashMap<Long, VariableAttributes>();
    variables.forEach(v -> varAttributes.put(v.getId(), v.getAttributes()));

    for (Variable var : variables) {
      AssignmentsTreeNode<AssignmentsTreeNodeData> varNode = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
        this, new VariableNodeData(var), parent);

      varNode.setType(
        assignments.isVariableComplete(var) ? VAR_FINISHED : VAR_UNFINISHED);

      for (SensorType sensorType : sensorConfig.getSensorTypes(var.getId(),
        true, true, true)) {

        AssignmentsTreeNode<AssignmentsTreeNodeData> node = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
          this, new SensorTypeNodeData(sensorType), varNode);

        boolean assigned = assignments.isAssigned(sensorType);

        boolean assignmentRequired = assigned
          || !assignments.isAssignmentRequired(sensorType, varAttributes);

        node.setType(
          assignmentRequired ? SENSOR_TYPE_ASSIGNED : SENSOR_TYPE_UNASSIGNED);

        if (assignments.isAssigned(sensorType)) {
          for (SensorAssignment assignment : assignments.get(sensorType)) {
            new AssignmentsTreeNode<AssignmentsTreeNodeData>(this, ASSIGNMENT,
              new SensorAssignmentNodeData(assignment), node);
          }
        }
      }
    }

    AssignmentsTreeNode<AssignmentsTreeNodeData> diagnosticsNode = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
      this, VAR_FINISHED, new StringNodeData("Diagnostics"), parent);

    for (SensorType diagnosticType : sensorConfig.getDiagnosticSensorTypes()) {
      AssignmentsTreeNode<AssignmentsTreeNodeData> node = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
        this, SENSOR_TYPE_ASSIGNED, new SensorTypeNodeData(diagnosticType),
        diagnosticsNode);

      if (assignments.isAssigned(diagnosticType)) {
        for (SensorAssignment assignment : assignments.get(diagnosticType)) {
          new AssignmentsTreeNode<AssignmentsTreeNodeData>(this, ASSIGNMENT,
            new SensorAssignmentNodeData(assignment), node);
        }
      }
    }
  }

  public abstract TreeNode<AssignmentsTreeNodeData> getRoot()
    throws AssignmentsTreeException;

  /**
   * Construct the date/time nodes for a file and add them to the specified
   * parent node.
   * 
   * @param file
   *          The file.
   * @param parent
   *          The parent node.
   * @throws DateTimeSpecificationException
   *           If the specification is invalid.
   */
  protected void buildDateTimeNodes(FileDefinitionBuilder file,
    TreeNode<AssignmentsTreeNodeData> parent)
    throws DateTimeSpecificationException {

    for (Map.Entry<String, Boolean> entry : file.getDateTimeSpecification()
      .getAssignedAndRequiredEntries().entrySet()) {

      if (entry.getValue()) {
        new AssignmentsTreeNode<AssignmentsTreeNodeData>(this,
          DATETIME_UNASSIGNED, new DateTimeNodeData(file, entry.getKey()),
          parent);
      } else {
        AssignmentsTreeNode<AssignmentsTreeNodeData> child = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
          this, DATETIME_ASSIGNED, new DateTimeNodeData(file, entry.getKey()),
          parent);

        DateTimeColumnAssignment assignment = file.getDateTimeSpecification()
          .getAssignment(
            DateTimeSpecification.getAssignmentIndex(entry.getKey()));

        new AssignmentsTreeNode<AssignmentsTreeNodeData>(this,
          DATETIME_ASSIGNMENT, new DateTimeAssignmentNodeData(file, assignment),
          child);
      }
    }
  }

  private AssignmentsTreeNode<AssignmentsTreeNodeData> makePositionNodes(
    String positionType, TreeNode<AssignmentsTreeNodeData> parent) {

    AssignmentsTreeNode<AssignmentsTreeNodeData> mainNode = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
      this, null, new StringNodeData(positionType), parent);

    String hemisphereNodeName = positionType + " Hemisphere";
    String unassignedType;
    String assignedType;
    String assignmentNodeType;

    for (int i = 0; i < files.size(); i++) {
      FileDefinitionBuilder file = (FileDefinitionBuilder) files.get(i);

      PositionSpecification posSpec;

      if (positionType.equals("Longitude")) {
        posSpec = file.getLongitudeSpecification();
        unassignedType = LONGITUDE_UNASSIGNED;
        assignedType = LONGITUDE_ASSIGNED;
        assignmentNodeType = LONGITUDE_ASSIGNMENT;
      } else {
        posSpec = file.getLatitudeSpecification();
        unassignedType = LATITUDE_UNASSIGNED;
        assignedType = LATITUDE_ASSIGNED;
        assignmentNodeType = LATITUDE_ASSIGNMENT;
      }

      if (posSpec.getValueColumn() > -1) {
        new AssignmentsTreeNode<AssignmentsTreeNodeData>(this,
          assignmentNodeType,
          new PositionSpecNodeData(file, posSpec, PositionSpecNodeData.VALUE),
          mainNode);
        mainNode.setType(assignedType);

        if (posSpec.hemisphereRequired()
          && posSpec.getHemisphereColumn() == -1) {
          new AssignmentsTreeNode<AssignmentsTreeNodeData>(this,
            HEMISPHERE_UNASSIGNED, new StringNodeData(hemisphereNodeName),
            mainNode);

          mainNode.setType(unassignedType);
        } else if (posSpec.getHemisphereColumn() > -1) {
          AssignmentsTreeNode<AssignmentsTreeNodeData> hemisphereNode = new AssignmentsTreeNode<AssignmentsTreeNodeData>(
            this, HEMISPHERE_ASSIGNED, new StringNodeData(hemisphereNodeName),
            mainNode);

          new AssignmentsTreeNode<AssignmentsTreeNodeData>(this,
            HEMISPHERE_ASSIGNMENT, new PositionSpecNodeData(file, posSpec,
              PositionSpecNodeData.HEMISPHERE),
            hemisphereNode);
        }
      } else {
        mainNode.setType(unassignedType);
      }
    }

    return mainNode;
  }

  public static AssignmentsTree create(int basis, NewInstrumentFileSet files,
    List<Variable> variables, SensorAssignments assignments,
    boolean needsPosition)
    throws SensorConfigurationException, SensorTypeNotFoundException {

    AssignmentsTree result = null;

    switch (basis) {
    case Instrument.BASIS_TIME: {
      result = new TimeBasisAssignmentsTree(files, variables, assignments,
        needsPosition);
      break;
    }
    case Instrument.BASIS_ARGO: {
      throw new NotImplementedException();
    }
    }

    return result;
  }

  public void nodeExpanded(NodeExpandEvent event) {
    expandStates.put(
      ((AssignmentsTreeNodeData) event.getTreeNode().getData()).getId(), true);
  }

  public void nodeCollapsed(NodeCollapseEvent event) {
    expandStates.put(
      ((AssignmentsTreeNodeData) event.getTreeNode().getData()).getId(), false);
  }

  public boolean getExpandState(AssignmentsTreeNodeData nodeData) {
    return expandStates.getOrDefault(nodeData.getId(), true);
  }
}
