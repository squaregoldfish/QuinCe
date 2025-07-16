package uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition;

import java.util.List;
import java.util.TreeSet;

import javax.sql.DataSource;

import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.DateTimeSpecification;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.LatitudeSpecification;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.LongitudeSpecification;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.PositionSpecification;
import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Specialised instance of {@link SensorAssignments} for Argo data.
 *
 * <p>
 * Includes assignment slots for the profile details (excluding time and
 * position, which are handled by {@link DateTimeSpecification} and
 * {@link PositionSpecification}.
 * </p>
 */
@SuppressWarnings("serial")
public class ArgoSensorAssignments extends SensorAssignments {

  protected ArgoSensorAssignments(DataSource dataSource, List<Long> variableIDs)
    throws SensorTypeNotFoundException, SensorConfigurationException,
    DatabaseException {

    super(dataSource, variableIDs);
    addProfileAssignments();
  }

  private void addProfileAssignments() throws SensorTypeNotFoundException {
    SensorsConfiguration sensorConfig = ResourceManager.getInstance()
      .getSensorsConfiguration();

    addProfileAssignment(sensorConfig.getSensorType("Cycle Number"));
    addProfileAssignment(sensorConfig.getSensorType("Profile"));
    addProfileAssignment(sensorConfig.getSensorType("Direction"));
    addProfileAssignment(sensorConfig.getSensorType("Level"));
    addProfileAssignment(sensorConfig.getSensorType("Pressure (Depth)"));
    addProfileAssignment(sensorConfig.getSensorType("Source File"));
  }

  private void addProfileAssignment(SensorType sensorType) {
    put(sensorType, new TreeSet<SensorAssignment>());
    forcedAssignmentRequired.put(sensorType, true);
  }

  @Override
  public int getFixedLongitudeFormat() {
    return LongitudeSpecification.FORMAT_MINUS180_180;
  }

  @Override
  public int getFixedLatitudeFormat() {
    return LatitudeSpecification.FORMAT_MINUS90_90;
  }
}
