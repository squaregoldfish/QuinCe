package uk.ac.exeter.QuinCe.data.Dataset;

import java.time.LocalDateTime;

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
   * Construct a copy of an existing TimeCoordinate with a new time.
   *
   * @param source
   *          The source coordinate.
   * @param newTime
   *          The new time.
   * @throws CoordinateException
   *           If the supplied {@link Coordinate} is not a TimeCoordinate.
   */
  public TimeCoordinate(Coordinate source, LocalDateTime newTime)
    throws CoordinateException {
    super(source.getId(), source.getDatasetId(), newTime);
    if (!(source instanceof TimeCoordinate)) {
      throw new CoordinateException("Must supply a TimeCoordinate");
    }
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
}
