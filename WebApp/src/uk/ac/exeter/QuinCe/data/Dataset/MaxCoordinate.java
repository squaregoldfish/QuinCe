package uk.ac.exeter.QuinCe.data.Dataset;

import java.time.LocalDateTime;

import uk.ac.exeter.QuinCe.utils.DatabaseUtils;

/**
 * A special instance of a {@link Coordinate} that is larger than all other
 * {@link Coordinate}s. It is used to represent Coordinate ranges with no end.
 *
 * <p>
 * This class is a singleton, and should only be accessed via
 * {@link Coordinate#MAX}.
 * </p>
 *
 * <p>
 * The IDs held in this Coordinate are invalid.
 * </p>
 */
public class MaxCoordinate extends Coordinate {

  private MaxCoordinate() {
    super(DatabaseUtils.NO_DATABASE_RECORD, DatabaseUtils.NO_DATABASE_RECORD);
  }

  private static MaxCoordinate instance = null;

  protected static MaxCoordinate getInstance() {
    if (null == instance) {
      instance = new MaxCoordinate();
    }

    return instance;
  }

  @Override
  public int getType() {
    return -1;
  }

  @Override
  protected int compareToWorker(Coordinate other) {
    if (other instanceof MaxCoordinate) {
      return 0;
    } else {
      return 1;
    }
  }

  @Override
  public LocalDateTime getTime() {
    return LocalDateTime.MAX;
  }
}
