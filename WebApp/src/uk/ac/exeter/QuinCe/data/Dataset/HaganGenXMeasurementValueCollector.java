package uk.ac.exeter.QuinCe.data.Dataset;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
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
        result = collectZeroMeasurementValues(instrument, measurement,
          allSensorValues);
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

  /**
   * Collect the {@link SensorValue}s required for a Zero measurement.
   * 
   * @param measurement
   *          The Zero measurement.
   * @param allSensorValues
   *          The Sensor Values.
   * @return The collected {@link SensorValue}s.
   * @throws RecordNotFoundException
   */
  private Collection<MeasurementValue> collectZeroMeasurementValues(
    Instrument instrument, Measurement measurement,
    DatasetSensorValues allSensorValues) throws Exception {

    ArrayList<MeasurementValue> measurementValues = new ArrayList<MeasurementValue>(
      2);

    LocalDateTime sensorValueTime = getSensorValueTime(measurement,
      allSensorValues);

    SensorType zeroCO2Raw1 = ResourceManager.getInstance()
      .getSensorsConfiguration().getSensorType("GenX Zero CO₂ Raw 1");
    SensorType zeroCO2Raw2 = ResourceManager.getInstance()
      .getSensorsConfiguration().getSensorType("GenX Zero CO₂ Raw 2");

    long raw1Col = instrument.getSensorAssignments().getColumnIds(zeroCO2Raw1)
      .get(0);
    long raw2Col = instrument.getSensorAssignments().getColumnIds(zeroCO2Raw2)
      .get(0);

    SensorValuesListOutput raw1 = allSensorValues.getColumnValues(raw1Col)
      .getValue(sensorValueTime, false);
    SensorValuesListOutput raw2 = allSensorValues.getColumnValues(raw2Col)
      .getValue(sensorValueTime, false);

    measurementValues.add(new MeasurementValue(zeroCO2Raw1, raw1));
    measurementValues.add(new MeasurementValue(zeroCO2Raw2, raw2));

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
  private LocalDateTime getSensorValueTime(Measurement measurement,
    DatasetSensorValues allSensorValues) {

    return allSensorValues.getTimes().stream()
      .filter(t -> DateTimeUtils.isEqualOrAfter(t, measurement.getTime()))
      .findFirst().orElse(null);
  }
}
