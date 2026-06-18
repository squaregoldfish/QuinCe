package uk.ac.exeter.QuinCe.data.Dataset;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Dataset.QC.IcosFlagScheme;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.VariablePropertiesException;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Custom {@link MeasurementValueCollector} for the Hagan GenX sensor.
 *
 * <p>
 * The timestamp of each {@link Measurement} does not align with its underlying
 * {@link SensorValue}s, due to the fact that multiple {@link Measurement}s are
 * represented on each line of the input file. (See
 * {@link HaganGenXMeasurementLocator} for a more thorough description.) This
 * {@link MeasurementValueCalculator} takes this into account when locating the
 * {@link SensorValue}s for each {@link Measurement}: because the timestamp of
 * the {@link SensorValue}s represents the end of a measurement cycle in the
 * sensor, the {@link SensorValue}s for a {@link Measurement} are found by
 * locating those which are immediately after the {@link Measurement}'s
 * timestamp.
 * </p>
 *
 * @see HaganGenXMeasurementLocator
 */
public class HaganGenXMeasurementValueCollector
  implements MeasurementValueCollector {

  private static final String[] ZERO_SENSOR_VALUES = new String[] {
    "GenX CO₂ Raw 1", "GenX CO₂ Raw 2", "Status Code" };

  private static final String[] NON_ZERO_SENSOR_VALUES = new String[] {
    "GenX Temperature", "GenX Pressure", "GenX CO₂ Raw 1", "GenX CO₂ Raw 2",
    "GenX Relative Humidity", "GenX Relative Humidity Temperature", "GenX CALK",
    "GenX Span Ref", "GenX Span Slope", "Status Code" };

  @Override
  public Collection<MeasurementValue> collectMeasurementValues(
    Instrument instrument, DataSet dataSet, Variable variable,
    DatasetMeasurements allMeasurements, DatasetSensorValues allSensorValues,
    Connection conn, Measurement measurement)
    throws MeasurementValueCollectorException {

    Collection<MeasurementValue> result;

    try {
      String runType = HaganGenXMeasurementLocator.getRunType(measurement);

      String[] sensorValues = runType
        .equals(HaganGenXMeasurementLocator.ZERO_RUN_TYPE) ? ZERO_SENSOR_VALUES
          : NON_ZERO_SENSOR_VALUES;

      result = collectMeasurementValues(instrument, measurement,
        allSensorValues, sensorValues);
    } catch (Exception e) {
      throw new MeasurementValueCollectorException(e);
    }

    return result;
  }

  private Collection<MeasurementValue> collectMeasurementValues(
    Instrument instrument, Measurement measurement,
    DatasetSensorValues allSensorValues, String[] sensorTypeNames)
    throws SensorValuesListException, RecordNotFoundException,
    SensorTypeNotFoundException, VariablePropertiesException {

    ArrayList<MeasurementValue> measurementValues = new ArrayList<MeasurementValue>(
      sensorTypeNames.length);

    TimeCoordinate sensorValueCord = getSensorValueTime(measurement,
      allSensorValues);

    for (String sensorTypeName : sensorTypeNames) {
      SensorType sensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType(sensorTypeName);

      long columnId = instrument.getSensorAssignments().getColumnIds(sensorType)
        .get(0);

      SensorValuesListOutput sensorValue = allSensorValues
        .getColumnValues(columnId).getValue(sensorValueCord, false);

      measurementValues.add(new MeasurementValue(IcosFlagScheme.getInstance(),
        sensorType, sensorValue));
    }

    return measurementValues;
  }

  /**
   * Get the timestamp for {@link SensorValue}s related to a specified
   * {@link Measurement}.
   *
   * <p>
   * Since {@link Measurement}s are offset from the timestamps in the original
   * file (which represents the end of the measurement cycle, the timestamp for
   * a {@link Measurement}'s {@link SensorValue}s is the one equal to or
   * immediately after the {@link Measurement}'s timestamp.
   * </p>
   *
   * @param measurement
   *          The {@link Measurement}.
   * @param allSensorValues
   *          The Sensor Values.
   * @return The timestamp for the {@link Measurement}'s {@link SensorValue}s.
   */
  private TimeCoordinate getSensorValueTime(Measurement measurement,
    DatasetSensorValues allSensorValues) {

    return (TimeCoordinate) allSensorValues
      .getCoordinates().stream().filter(t -> DateTimeUtils
        .isEqualOrAfter(t.getTime(), measurement.getCoordinate().getTime()))
      .findFirst().orElse(null);
  }
}
