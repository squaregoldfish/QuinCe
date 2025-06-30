package uk.ac.exeter.QuinCe.data.Dataset;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;

/**
 * Represents a newly created collection of {@link SensorValue}s which have not
 * yet been stored in the database.
 *
 * <p>
 * {@link SensorValue} objects are not passed in to this object; instead, the
 * values for a new {@link SensorValue} are passed in, and the class takes care
 * of creating the objects. The created objects can be retrieved by calling
 * {@link #toSet()}.
 * </p>
 *
 * <p>
 * Note that this is not a Java {@link Collection}.
 * </p>
 */
public class NewSensorValues {

  /**
   * The {@link DataSet} that the {@SensorValue}s belong to.
   */
  private DataSet dataset;

  /**
   * A cache of previously generated {@link Coordinate} objects.
   */
  private HashMap<Coordinate, Coordinate> coordinates = new HashMap<Coordinate, Coordinate>();

  /**
   * The {@link SensorValue} objects.
   */
  private TreeSet<SensorValue> sensorValues = new TreeSet<SensorValue>();

  /**
   * Initialise an empty object.
   *
   * @param dataset
   *          The {@link DataSet} that the {@link SensorValue}s belong to.
   * @throws CoordinateException
   * @throws RecordNotFoundException
   * @throws DatabaseException
   */
  public NewSensorValues(DataSet dataset)
    throws DatabaseException, RecordNotFoundException, CoordinateException {
    this.dataset = dataset;
  }

  public SensorValue create(long columnId, LocalDateTime time, String value) {

    /*
     * Generate the Coordinate object for the {@link SensorValue}.
     *
     * If a Coordinate already exists, use that instance.
     */
    Coordinate coordinate = new TimeCoordinate(dataset.getId(), time);
    if (coordinates.containsKey(coordinate)) {
      coordinate = coordinates.get(coordinate);
    } else {
      coordinates.put(coordinate, coordinate);
    }

    SensorValue sensorValue = new SensorValue(dataset.getId(), columnId,
      coordinate, value);

    sensorValues.add(sensorValue);

    return sensorValue;
  }

  public TreeSet<SensorValue> toSet() {
    return sensorValues;
  }

  /**
   * Get the number of {@link SensorValue}s in the collection.
   *
   * @return
   */
  public int size() {
    return sensorValues.size();
  }

  public Iterator<SensorValue> iterator() {
    return sensorValues.iterator();
  }

  /**
   * Clear the {@code dirty} flags on all {@link SensorValue}s.
   */
  public void clearDirtyFlags() {
    // Clear the dirty flag on all the sensor values
    SensorValue.clearDirtyFlag(sensorValues);
  }

  public Collection<Coordinate> getCoordinates() {
    return coordinates.values();
  }

  public Collection<SensorValue> getSensorValues() {
    return Collections.unmodifiableCollection(sensorValues);
  }

}
