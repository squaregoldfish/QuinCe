package uk.ac.exeter.QuinCe.data.Dataset;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.ExternalStandardDB;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorsConfiguration;
import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * The default implementation of {@link MeasurementValueCalculator}.
 * 
 * <p>
 * Applies interpolation based on quality flags, and standard calibration as
 * needed.
 * </p>
 * 
 * @author stevej
 *
 */
public class DefaultMeasurementValueCalculator
  extends MeasurementValueCalculator {

  // TODO Need limits on how far interpolation goes before giving up.

  @Override
  public MeasurementValue calculate(Instrument instrument,
    Measurement measurement, SensorType sensorType,
    DatasetMeasurements allMeasurements, DatasetSensorValues allSensorValues,
    Connection conn) throws MeasurementValueCalculatorException {

    try {
      // TODO #1128 This currently assumes only one sensor for each SensorType.
      // This will have to change eventually.

      // Get the file column for the sensor type
      long columnId = instrument.getSensorAssignments().getColumnIds(sensorType)
        .get(0);

      // If the required SensorType is core, we do not search for interpolated
      // values - the core sensor type defines the measurement so must be used.
      SensorsConfiguration sensorConfig = ResourceManager.getInstance()
        .getSensorsConfiguration();

      SearchableSensorValuesList sensorValues = allSensorValues
        .getColumnValues(columnId);

      MeasurementValue result = new MeasurementValue(sensorType);

      if (sensorConfig.isCoreSensor(sensorType)) {
        SensorValue sensorValue = sensorValues.get(measurement.getTime());
        if (null != sensorValue) {
          result.addSensorValue(sensorValue);
          result.setCalculatedValue(sensorValue.getDoubleValue());
          result.setMemberCount(1);
        }
      } else {

        // Otherwise we get the closest GOOD (or best available quality) values
        // we can, interpolating where required.
        List<SensorValue> valuesToUse = sensorValues
          .getWithInterpolation(measurement.getTime(), true);

        switch (valuesToUse.size()) {
        case 0: {
          // We should not use a value here
          result.setCalculatedValue(Double.NaN);
          result.setMemberCount(0);
          break;
        }
        case 1: {
          // Value from exact time - use it directly
          result.addSensorValue(valuesToUse.get(0));
          result.setCalculatedValue(valuesToUse.get(0).getDoubleValue());
          result.setMemberCount(1);
          break;
        }
        case 2: {
          result.addSensorValues(valuesToUse);
          result.setCalculatedValue(interpolate(valuesToUse.get(0),
            valuesToUse.get(1), measurement.getTime()));
          result.setMemberCount(1);
          break;
        }
        default: {
          throw new MeasurementValueCalculatorException(
            "Invalid number of values in search result");
        }
        }
      }

      if (sensorType.hasInternalCalibration()) {
        calibrate(instrument, measurement, sensorType, result, allMeasurements,
          sensorValues, conn);
      }

      return result;
    } catch (DatabaseException e) {
      throw new MeasurementValueCalculatorException(
        "Error getting sensor value details", e);
    }
  }

  private void calibrate(Instrument instrument, Measurement measurement,
    SensorType sensorType, MeasurementValue value,
    DatasetMeasurements allMeasurements,
    SearchableSensorValuesList sensorValues, Connection conn)
    throws MeasurementValueCalculatorException {

    if (!value.getCalculatedValue().isNaN()) {

      try {
        CalibrationSet calibrationSet = ExternalStandardDB.getInstance()
          .getMostRecentCalibrations(conn, instrument.getDatabaseId(),
            measurement.getTime());

        // Get the standards closest to the measured value, with their
        // concentrations.
        Map<String, Double> closestStandards = calibrationSet
          .getClosestStandards(sensorType, value.getCalculatedValue());

        if (closestStandards.size() < 3) {
          value.addQC(Flag.BAD, "Fewer than 3 standards used for calibration");
        }

        // For each external standard target, calculate the offset from the
        // external standard at the measurement time.
        //
        // We get the offset at the prior and post measurements of that
        // standard,
        // and then interpolate to get the offset at the measurement time.
        Map<String, Double> standardOffsets = new HashMap<String, Double>();

        for (Map.Entry<String, Double> standard : closestStandards.entrySet()) {
          String target = standard.getKey();
          double standardConcentration = standard.getValue();

          SensorValue priorCalibrationValue = getPriorCalibrationValue(
            measurement.getTime(), allMeasurements.getMeasurements(target),
            sensorValues);

          SensorValue postCalibrationValue = getPostCalibrationValue(
            measurement.getTime(), allMeasurements.getMeasurements(target),
            sensorValues);

          LocalDateTime priorTime = null;
          Double priorOffset = null;

          if (null != priorCalibrationValue) {
            priorTime = priorCalibrationValue.getTime();
            priorOffset = priorCalibrationValue.getDoubleValue()
              - standardConcentration;
            value.addSupportingSensorValue(priorCalibrationValue);
          }

          LocalDateTime postTime = null;
          Double postOffset = null;

          if (null != postCalibrationValue) {
            postTime = postCalibrationValue.getTime();
            postOffset = postCalibrationValue.getDoubleValue()
              - standardConcentration;
            value.addSupportingSensorValue(postCalibrationValue);
          }

          standardOffsets.put(target, interpolate(priorTime, priorOffset,
            postTime, postOffset, measurement.getTime()));
        }

        // Make a regression of the offsets to calculate the offset at the
        // measurement time
        SimpleRegression regression = new SimpleRegression(true);
        for (String target : standardOffsets.keySet()) {
          regression.addData(calibrationSet.getCalibrationValue(target,
            value.getSensorType().getName()), standardOffsets.get(target));
        }

        double calibrationOffset = regression
          .predict(value.getCalculatedValue());

        // Now apply the offset to the measured value.
        // TODO #732/#410 Add excessive calibration adjustment check to this
        // method - it will set the flag on the MeasurementValue. Needs to be
        // defined per sensor type.
        value
          .setCalculatedValue(value.getCalculatedValue() - calibrationOffset);

      } catch (Exception e) {
        throw new MeasurementValueCalculatorException(
          "Error while calculating calibrated value", e);
      }
    }
  }

  /**
   * Get the closest GOOD calibration value before a given time.
   * 
   * @param startTime
   *          The start time.
   * @param measurements
   *          The list of measurements from which to select are value. Assumed
   *          to be correct for the desired calibration target.
   * @param sensorValues
   *          The list of sensor values for the desired data column.
   * @return The SensorValue for the calibration measurement.
   */
  private SensorValue getPriorCalibrationValue(LocalDateTime startTime,
    List<Measurement> measurements, SearchableSensorValuesList sensorValues) {

    // Work out where we're starting in the list of measurements for the target
    // standard.
    // The result of this search should be negative because our base measurement
    // will not be in the calibration run type. But we handle the case where
    // it's positive just in case. (See documentation for binarySearch.)

    int startPoint = Collections.binarySearch(measurements,
      Measurement.dummyTimeMeasurement(startTime), Measurement.TIME_COMPARATOR);

    if (startPoint >= 0) {
      startPoint--;
    } else {
      startPoint = (startPoint * -1) - 2;
    }

    SensorValue result = null;

    int searchPoint = startPoint;
    while (searchPoint >= 0) {
      LocalDateTime testTime = measurements.get(startPoint).getTime();
      SensorValue testValue = sensorValues.get(testTime);
      if (null != testValue && testValue.getUserQCFlag().isGood()) {
        result = testValue;
        break;
      }

      searchPoint--;
    }

    return result;
  }

  /**
   * Get the closest GOOD calibration value after a given time.
   * 
   * @param startTime
   *          The start time.
   * @param measurements
   *          The list of measurements from which to select are value. Assumed
   *          to be correct for the desired calibration target.
   * @param sensorValues
   *          The list of sensor values for the desired data column.
   * @return The SensorValue for the calibration measurement.
   */
  private SensorValue getPostCalibrationValue(LocalDateTime startTime,
    List<Measurement> measurements, SearchableSensorValuesList sensorValues) {

    // Work out where we're starting in the list of measurements for the target
    // standard.
    // The result of this search should be negative because our base measurement
    // will not be in the calibration run type. But we handle the case where
    // it's positive just in case. (See documentation for binarySearch.)

    int startPoint = Collections.binarySearch(measurements,
      Measurement.dummyTimeMeasurement(startTime), Measurement.TIME_COMPARATOR);

    if (startPoint >= 0) {
      startPoint++;
    } else {
      startPoint = (startPoint * -1) - 1;
    }

    SensorValue result = null;

    int searchPoint = startPoint;
    while (searchPoint < measurements.size()) {
      LocalDateTime testTime = measurements.get(startPoint).getTime();
      SensorValue testValue = sensorValues.get(testTime);
      if (null != testValue && testValue.getUserQCFlag().isGood()) {
        result = testValue;
        break;
      }

      searchPoint++;
    }

    return result;
  }
}