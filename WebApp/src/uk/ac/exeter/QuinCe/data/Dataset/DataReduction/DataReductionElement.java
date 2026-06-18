package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValue;
import uk.ac.exeter.QuinCe.data.Dataset.TimeCoordinate;

/**
 * Holds details of a complex intermediate value created during data reduction.
 *
 * <p>
 * Some {@link DataReducer}s have to create intermediate values based on other
 * extra {@link SensorValue}s to those directly involved in a give
 * {@link Measurement}. For example, the {@link HaganGenXEqReducer} creates
 * specialised calibration data during its
 * {@link HaganGenXEqReducer#preprocess(java.sql.Connection, uk.ac.exeter.QuinCe.data.Instrument.Instrument, uk.ac.exeter.QuinCe.data.Dataset.DataSet, uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues, uk.ac.exeter.QuinCe.data.Dataset.DatasetMeasurements)}
 * method. This class holds the result of such an intermediate value calculation
 * along with the {@link SensorValue} IDs used to generate it. These can then be
 * added to a {@link MeasurementValue}'s supporting sensor value IDs
 * ({@link MeasurementValue#getSupportingSensorValueIds()} to maintain complete
 * data provenance.
 * </p>
 *
 * <p>
 * The {@link SensorValue}s are stored as a {@link Set} to prevent duplicates.
 * </p>
 */
public class DataReductionElement implements Cloneable {

  private Double value = Double.NaN;

  private Set<Long> sensorValueIDs = new HashSet<Long>();

  public void setValue(Double value) {
    this.value = value;
  }

  public void addSensorValue(SensorValue sensorValue) {
    sensorValueIDs.add(sensorValue.getId());
  }

  public void addSensorValueIDs(Collection<Long> ids) {
    sensorValueIDs.addAll(ids);
  }

  public Double getValue() {
    return value;
  }

  public Set<Long> getSensorValueIDs() {
    return sensorValueIDs;
  }

  public void addSensorValueIDs(MeasurementValue... measurementValues) {
    for (MeasurementValue measurementValue : measurementValues) {
      addSensorValueIDs(measurementValue.getSensorValueIds());
      addSensorValueIDs(measurementValue.getSupportingSensorValueIds());
    }
  }

  public void addSensorValueIDs(DataReductionElement dataReductionElement) {
    addSensorValueIDs(dataReductionElement.getSensorValueIDs());
  }

  @Override
  public Object clone() {
    DataReductionElement clone = new DataReductionElement();
    clone.setValue(value);
    clone.addSensorValueIDs(sensorValueIDs);
    return clone;
  }

  /**
   * Interpolate the two closest {@code DataReductionElement} objects in a
   * collection to the specified {@link TimeCoordinate}.
   *
   * <p>
   * The method returns a new {@code DataReductionElement} whose Sensor Value
   * IDs are the combined collection of IDs from the interpolated
   * {@code DataReductionElements}.
   * </p>
   *
   * @param coordinate
   *          The target coordinate.
   * @param elements
   *          The elements to interpolate
   * @return The interpolated element.
   * @throws DataReductionException
   */
  public static DataReductionElement getInterpolatedElement(
    TimeCoordinate coordinate,
    TreeMap<TimeCoordinate, DataReductionElement> elements)
    throws DataReductionException {

    DataReductionElement result;

    if (elements.containsKey(coordinate)) {
      result = (DataReductionElement) elements.get(coordinate).clone();
    } else {
      Map.Entry<TimeCoordinate, DataReductionElement> prior = elements
        .floorEntry(coordinate);
      Map.Entry<TimeCoordinate, DataReductionElement> post = elements
        .ceilingEntry(coordinate);

      if (null == post) {
        result = (DataReductionElement) prior.getValue().clone();
      } else if (null == prior) {
        result = (DataReductionElement) post.getValue().clone();
      } else {
        LocalDateTime priorTime = prior.getKey().getTime();
        LocalDateTime postTime = post.getKey().getTime();
        Double priorValue = prior.getValue().getValue();
        Double postValue = post.getValue().getValue();

        Double interpolatedValue = Calculators.interpolate(priorTime,
          priorValue, postTime, postValue, coordinate.getTime());

        result = new DataReductionElement();
        result.setValue(interpolatedValue);
        result.addSensorValueIDs(prior.getValue().getSensorValueIDs());
        result.addSensorValueIDs(post.getValue().getSensorValueIDs());
      }
    }

    return result;
  }
}
