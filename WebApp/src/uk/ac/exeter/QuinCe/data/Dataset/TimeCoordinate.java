package uk.ac.exeter.QuinCe.data.Dataset;

import java.time.LocalDateTime;
import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.utils.DatabaseUtils;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;

/**
 * Coordinate for time-based measurements.
 *
 * @see uk.ac.exeter.QuinCe.data.Instrument.Instrument#BASIS_TIME
 */
public class TimeCoordinate extends Coordinate {

  /**
   * Constructor. Time is required.
   *
   * @param id
   *          The coordinate's database ID
   * @param datasetId
   *          The database ID of the {@link DataSet} that the coordinate belongs
   *          to.
   * @param time
   *          The timestamp.
   * @throws CoordinateException
   *           If the timestamp is null.
   */
  public TimeCoordinate(long id, long datasetId, LocalDateTime time) {
    super(id, datasetId, time);
    if (null == time) {
      throw new NullPointerException("Time cannot be null");
    }
  }

  /**
   * Construct a TimeCoordinate for a DataSet with a specified time.
   *
   * <p>
   * The coordinate will not exist in the database, so will be given an ID of
   * {@link DatabaseUtils#NO_DATABASE_RECORD}.
   * </p>
   *
   * @param datasetId
   *          The DataSet ID.
   * @param time
   *          The time.
   */
  private TimeCoordinate(long datasetId, LocalDateTime time)
    throws CoordinateException {
    super(DatabaseUtils.NO_DATABASE_RECORD, datasetId, time);
  }

  @Override
  public int getType() {
    return Instrument.BASIS_TIME;
  }

  @Override
  protected int compareToWorker(Coordinate o) {
    if (!(o instanceof TimeCoordinate)) {
      throw new IllegalArgumentException(
        "Cannot compare Coordinates of different types.");
    }

    return getTime().compareTo(o.getTime());
  }

  @Override
  public String toString() {
    return DateTimeUtils.formatDateTime(getTime());
  }

  /**
   * Generate a dummy TimeCoordinate with the specified time.
   *
   * <p>
   * The result of calling {@link #getId()} and {@link #getDatasetId()} on the
   * returned object will be {@link DatabaseUtils#NO_DATABASE_RECORD}. *
   * </p>
   *
   * @param time
   *          The time for the coordinate.
   * @return The coordinate.
   */
  public static TimeCoordinate dummyCoordinate(LocalDateTime time) {
    return new TimeCoordinate(DatabaseUtils.NO_DATABASE_RECORD,
      DatabaseUtils.NO_DATABASE_RECORD, time);
  }

  /**
   * Get a TimeCoordinate for a specified timestamp.
   *
   * <p>
   * The existing coordinates for the DataSet are supplied in
   * {@code existingCoordinates}. If a coordinate exists with the specified
   * time, that coordinate is returned. Otherwise a new {@link TimeCoordinate}
   * object is created with the specified time. Calling {@link #isInDatabase()}
   * on this new coordinate will return {@code false}.
   * </p>
   *
   * @param time
   *          The desired coordinate time.
   * @param existingCoordinates
   *          The existing coordinates in the DataSet.
   * @return The found or created coordinate.
   * @throws CoordinateException
   *           If a new coordinate is required but cannot be created.
   */
  public static TimeCoordinate getCoordinate(LocalDateTime time,
    Collection<Coordinate> existingCoordinates) throws CoordinateException {

    return (TimeCoordinate) existingCoordinates.stream()
      .filter(c -> c.getTime().equals(time)).findAny()
      .orElse(new TimeCoordinate(
        existingCoordinates.stream().findFirst().get().getDatasetId(), time));
  }
}
