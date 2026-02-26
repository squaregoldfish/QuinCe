package uk.ac.exeter.QuinCe.data.Dataset;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorsConfiguration;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Custom {@link MeasurementLocator} for the Hagan GenX sensor.
 * 
 * <p>
 * Each row in the input file contains Water and Air measurements, and sometimes
 * Zero and Span measurements. There is only one 'real' timestamp on each line,
 * which represents the end of the measurement cycle in the sensor. The
 * sub-measurement times are defined in separate columns as UNIX timestamps.
 * This locator extracts each of the sub-measurements into their own
 * {@link Measurement} object with the correct timestamp for the
 * sub-measurement, and a corresponding Run Type.
 * </p>
 * 
 * <p>
 * The {@link HaganGenXMeasurementValueCollector} will take account of the fact
 * that the {@link Measurement} timestamps do not line up with the 'real'
 * timestamp, which is assigned to all the {@link SensorValue}s.
 * </p>
 * 
 * @see HaganGenXMeasurementValueCollector
 */
public class HaganGenXMeasurementLocator extends MeasurementLocator {

  public static final String ZERO_RUN_TYPE = "ZERO";

  public static final String SPAN_RUN_TYPE = "SPAN";

  public static final String WATER_RUN_TYPE = "EQU";

  public static final String AIR_RUN_TYPE = "AIR";

  private HashMap<Long, String> zeroRunTypes;

  private HashMap<Long, String> spanRunTypes;

  private HashMap<Long, String> equRunTypes;

  private HashMap<Long, String> airRunTypes;

  private long zeroTimeCol = -1L;

  private long zeroCO2Raw1Col = -1L;

  static {

  }

  @Override
  public List<Measurement> locateMeasurements(Connection conn,
    Instrument instrument, DataSet dataset, DatasetSensorValues allSensorValues)
    throws MeasurementLocatorException {

    try {
      SensorsConfiguration sensorConfig = ResourceManager.getInstance()
        .getSensorsConfiguration();

      Variable variable = sensorConfig.getInstrumentVariable(getVariableName());

      // Run types for the different measurement modes
      zeroRunTypes = new HashMap<Long, String>();
      zeroRunTypes.put(variable.getId(), ZERO_RUN_TYPE);

      spanRunTypes = new HashMap<Long, String>();
      spanRunTypes.put(variable.getId(), SPAN_RUN_TYPE);

      equRunTypes = new HashMap<Long, String>();
      equRunTypes.put(variable.getId(), WATER_RUN_TYPE);

      airRunTypes = new HashMap<Long, String>();
      airRunTypes.put(variable.getId(), AIR_RUN_TYPE);

      List<Measurement> measurements = new ArrayList<Measurement>();

      // Get Column IDs
      zeroTimeCol = instrument.getSensorAssignments()
        .getColumnIds("GenX Zero Timestamp").get(0);

      zeroCO2Raw1Col = instrument.getSensorAssignments()
        .getColumnIds("GenX Zero CO₂ Raw 1").get(0);

      // Loop through all times
      for (LocalDateTime time : allSensorValues.getTimes()) {
        Map<Long, SensorValue> sensorValues = allSensorValues.get(time);

        // See if there was a Zero measurement
        if (sensorValues.containsKey(zeroCO2Raw1Col)) {
          measurements.add(makeZeroMeasurement(dataset, sensorValues));
        }

      }
      return measurements;
    } catch (Exception e) {
      throw new MeasurementLocatorException(e);
    }
  }

  /**
   * Create a Zero measurement.
   * 
   * <p>
   * This method assumes that the caller has already verified the existence of
   * the required columns for a Zero measurement.
   * </p>
   * 
   * @param dataset
   *          The DataSet.
   * @param sensorValues
   *          The Sensor Values being processed.
   * @return The Zero measurement.
   */
  private Measurement makeZeroMeasurement(DataSet dataset,
    Map<Long, SensorValue> sensorValues) {

    LocalDateTime time = DateTimeUtils
      .longToDate(sensorValues.get(zeroTimeCol).getValue());

    return new Measurement(dataset.getId(), time, zeroRunTypes);
  }

  protected String getVariableName() {
    return "Hagan GenX";
  }

}
