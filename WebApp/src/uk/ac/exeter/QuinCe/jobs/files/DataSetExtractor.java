package uk.ac.exeter.QuinCe.jobs.files;

import java.sql.Connection;
import java.util.Set;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.GeoBounds;
import uk.ac.exeter.QuinCe.data.Dataset.NewSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValue;
import uk.ac.exeter.QuinCe.data.Files.DataFile;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;

/**
 * A data extraction worker implementation, used by {@link ExtractDataSetJob}.
 */
public abstract class DataSetExtractor {

  protected Set<DataFile> usedFiles = null;

  protected NewSensorValues sensorValues = null;

  protected GeoBounds geoBounds = null;

  /**
   * Perform the extraction work on the supplied {@link DataSet}.
   *
   * @param conn
   *          A database connection.
   * @param instrument
   *          The {@link Insturment} to which the {@link DataSet} belongs.
   * @param dataSet
   *          The {@link DataSet} to be processed.
   */
  public abstract void extract(Connection conn, Instrument instrument,
    DataSet dataSet) throws Exception;

  /**
   * Retrieve the source {@link DataFiles} which are used in the
   * {@link DataSet}.
   *
   * @return The used files.
   */
  public Set<DataFile> getUsedFiles() {
    return usedFiles;
  }

  /**
   * Get the extracted {@link SensorValue}s.
   *
   * @return The sensor values.
   */
  public NewSensorValues getSensorValues() {
    return sensorValues;
  }

  /**
   * Get the geographical bounds of the dataset.
   *
   * <p>
   * May be null if the dataset contains no geographical data.
   * </p>
   *
   * @return The bounds.
   */
  public GeoBounds getGeoBounds() {
    return geoBounds;
  }
}
