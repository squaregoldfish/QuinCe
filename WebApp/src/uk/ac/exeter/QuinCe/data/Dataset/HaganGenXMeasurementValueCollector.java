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
    "GenX Zero CO₂ Raw 1", "GenX Zero CO₂ Raw 2" };

  private static final String[] SPAN_SENSOR_VALUES = new String[] {
    "GenX Span Temperature", "GenX Span Pressure", "GenX Span CO₂ Raw 1",
    "GenX Span CO₂ Raw 2", "GenX Span Relative Humidity",
    "GenX Span Relative Humidity Temperature" };

  private static final String[] EQU_SENSOR_VALUES = new String[] {
    "GenX Water Temperature", "GenX Water Pressure", "GenX Water CO₂ Raw 1",
    "GenX Water CO₂ Raw 2", "GenX Water Relative Humidity",
    "GenX Water Relative Humidity Temperature" };

  private static final String[] AIR_SENSOR_VALUES = new String[] {
    "GenX Air Timestamp", "GenX Air Temperature", "GenX Air Pressure",
    "GenX Air CO₂ Raw 1", "GenX Air CO₂ Raw 2", "GenX Air Relative Humidity",
    "GenX Air Relative Humidity Temperature" };

  @Override
  public Collection<MeasurementValue> collectMeasurementValues(
    Instrument instrument, DataSet dataSet, Variable variable,
    DatasetMeasurements allMeasurements, DatasetSensorValues allSensorValues,
    Connection conn, Measurement measurement)
    throws MeasurementValueCollectorException {

    Collection<MeasurementValue> result;

    try {
      switch (measurement.getRunType(variable)) {
      case HaganGenXMeasurementLocator.ZERO_RUN_TYPE: {
        result = collectMeasurementValues(instrument, measurement,
          allSensorValues, ZERO_SENSOR_VALUES);
        break;
      }
      case HaganGenXMeasurementLocator.SPAN_RUN_TYPE: {
        result = collectMeasurementValues(instrument, measurement,
          allSensorValues, SPAN_SENSOR_VALUES);
        break;
      }
      case HaganGenXMeasurementLocator.WATER_RUN_TYPE: {
        result = collectMeasurementValues(instrument, measurement,
          allSensorValues, EQU_SENSOR_VALUES);
        break;
      }
      case HaganGenXMeasurementLocator.AIR_RUN_TYPE: {
        result = collectMeasurementValues(instrument, measurement,
          allSensorValues, AIR_SENSOR_VALUES);
        break;
      }
      default: {
        throw new MeasurementValueCollectorException(
          "Unrecognised Run Type '" + measurement.getRunType(variable) + "'");
      }
      }
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
