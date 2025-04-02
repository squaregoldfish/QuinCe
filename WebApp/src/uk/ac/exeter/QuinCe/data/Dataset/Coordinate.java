package uk.ac.exeter.QuinCe.data.Dataset;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.sound.midi.Instrument;

import uk.ac.exeter.QuinCe.utils.DatabaseUtils;
import uk.ac.exeter.QuinCe.utils.MissingParam;

/**
 * Holds a coordinate that is the basic index for a {@link SensorValue} or
 * {@link Measurement}.
 *
 * <p>
 * A coordinate represents a 'point' at which something is measured. Every
 * {@link SensorValue} and {@link Measurement} must be linked to a
 * {@link Coordinate}.
 * </p>
 *
 * <p>
 * The contents of a {@link Coordinate} will depend on the measurement basis of
 * the {@link Instrument}. Surface measurements require only a timestamp, Argo
 * measurements require a cycle number and depth, etc. Subclasses of this class
 * will be used for the different coordinate types.
 * </p>
 *
 * @see uk.ac.exeter.QuinCe.data.Instrument.Instrument#getBasis()
 */
public abstract class Coordinate implements Comparable<Coordinate> {

  /**
   * A special instance of a {@link Coordinate} that is larger than all other
   * {@link Coordinate}s.
   */
  public static final Coordinate MAX = MaxCoordinate.getInstance();

  /**
   * The coordinate's database ID.
   */
  private long id;

  /**
   * The ID of the {@link DataSet} that this coordinate is part of.
   */
  private final long datasetId;

  /**
   * The timestamp of the coordinate. Optional for most coordinate types.
   */
  private LocalDateTime time = null;

  public Coordinate(long id, long datasetId) {
    MissingParam.checkPositive(datasetId, "datasetId");
    this.id = id;
    this.datasetId = datasetId;
  }

  public Coordinate(long id, long datasetId, LocalDateTime time) {
    MissingParam.checkPositive(datasetId, "datasetId");
    this.id = id;
    this.datasetId = datasetId;
    this.time = time;
  }

  /**
   * Get the coordinate's database ID.
   *
   * @return The database ID.
   */
  public long getId() {
    return id;
  }

  /**
   * Get the database ID of the {@link DataSet} that this coordinate is part of.
   *
   * @return The dataset ID
   */
  public long getDatasetId() {
    return datasetId;
  }

  /**
   * Return the timestamp of this coordinate.
   *
   * <p>
   * Can be {@code null} depending on the coordinate type.
   * </p>
   *
   * @return The timestamp.
   */
  public LocalDateTime getTime() {
    return time;
  }

  /**
   * Set the database ID for the coordinate.
   *
   * <p>
   * This should only be called when the coordinate is first stored in the
   * database. It cannot be called if the existing ID is anything other than
   * {@link DatabaseUtils#NO_DATABASE_RECORD}.
   * </p>
   *
   * @param id
   *          The database ID
   * @throws CoordinateException
   *           If the coordinate already has an ID.
   */
  protected void setId(long id) throws CoordinateException {
    if (id != DatabaseUtils.NO_DATABASE_RECORD) {
      throw new CoordinateException("Coordinate already has a database id");
    }
    this.id = id;
  }

  /**
   * Get the coordinate type.
   *
   * <p>
   * Corresponds to a measurement basis.
   * </p>
   *
   * @return The coordinate type.
   * @see uk.ac.exeter.QuinCe.data.Instrument.Instrument#getBasis()
   */
  public abstract int getType();

  @Override
  public final int compareTo(Coordinate other) {
    if (getClass() == other.getClass()) {
      int result = Long.compare(datasetId, other.datasetId);
      if (result == 0) {
        result = this.compareToWorker(other);
      }
      return result;
    } else {
      throw new IllegalArgumentException(
        "Cannot compare coordinates of different types");
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasetId, id, time);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Coordinate other = (Coordinate) obj;
    return datasetId == other.datasetId && id == other.id
      && Objects.equals(time, other.time);
  }

  protected abstract int compareToWorker(Coordinate other);

  public boolean isBefore(Coordinate other) {
    return compareTo(other) < 0;
  }

  public boolean isAfter(Coordinate other) {
    return compareTo(other) > 0;
  }

  /**
   * Determine whether or not this TimeCoordinate has been stored in the
   * database.
   *
   * @return {@code true} if the coordinate is in the database; {@code false}
   *         otherwise.
   */
  public boolean isInDatabase() {
    return getId() != DatabaseUtils.NO_DATABASE_RECORD;
  }
}
