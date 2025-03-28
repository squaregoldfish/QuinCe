package uk.ac.exeter.QuinCe.data.Dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.utils.DatabaseUtils;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;

/**
 * Methods for storing and accessing {@link Coordinate} objects in the database.
 */
public class CoordinateDB {

  private static final String STORE_SURFACE_COORDINATE_STMT = "INSERT INTO coordinates "
    + "(dataset_id, date) VALUES (?, ?)";

  /**
   * Store the provided {@link Coordinate}s in the database.
   * 
   * <p>
   * A {@link Coordinate} will only be saved if it does not already have a
   * database ID; otherwise it will be ignored. {@link Coordinate}s without
   * database IDs will be stored, and their {@code id}s updated with the
   * generated database keys.
   * </p>
   * 
   * <p>
   * <b>Note:</b> It is usually desirable that this method is called as part of
   * a larger transaction. It is up to the caller to work this out: this method
   * does not change the commit status of the supplied {@link Connection}, nor
   * does it perform any explicit commit action.
   * </p>
   * 
   * @param conn
   *          A database connection.
   * @param coordinates
   *          The coordinates to be stored.
   * @throws CoordinateException
   *           If the coordinates are not all of the same type, or any
   *           coordinate is invalid.
   * @throws DatabaseException
   *           If a database error occurs.
   */
  protected static void saveCoordinates(Connection conn,
    Collection<Coordinate> coordinates)
    throws CoordinateException, DatabaseException {

    if (coordinates.size() > 0) {
      // Make sure all coordinates are of the same type
      if (coordinates.stream().map(c -> c.getType()).distinct().limit(2)
        .count() > 1) {
        throw new CoordinateException(
          "All coordinates must be of the same type");
      }

      switch (coordinates.stream().findAny().get().getType()) {
      case Instrument.BASIS_TIME: {
        storeSurfaceCoordinates(conn, coordinates);
        break;
      }
      case Instrument.BASIS_ARGO: {
        throw new NotImplementedException(
          "Argo coordinates not yet implemented");
      }
      default: {
        throw new CoordinateException("Unrecognised coordinate type");
      }
      }
    }
  }

  /**
   * Store the provided surface coordinates in the database
   * 
   * @param conn
   * @param coordinates
   * @throws DatabaseException
   * @throws CoordinateException
   */
  private static void storeSurfaceCoordinates(Connection conn,
    Collection<Coordinate> coordinates)
    throws DatabaseException, CoordinateException {

    try (PreparedStatement stmt = conn.prepareStatement(
      STORE_SURFACE_COORDINATE_STMT, Statement.RETURN_GENERATED_KEYS)) {

      for (Coordinate coordinate : coordinates) {
        if (coordinate.getId() == DatabaseUtils.NO_DATABASE_RECORD) {
          stmt.setLong(1, coordinate.getDatasetId());
          stmt.setLong(2, DateTimeUtils.dateToLong(coordinate.getTime()));

          stmt.execute();

          try (ResultSet keys = stmt.getGeneratedKeys()) {
            keys.next();
            coordinate.setId(keys.getLong(1));
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Error while storing coordinates", e);
    }
  }
}
