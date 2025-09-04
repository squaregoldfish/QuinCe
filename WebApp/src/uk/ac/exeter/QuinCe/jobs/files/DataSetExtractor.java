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
public interface DataSetExtractor {

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
  public void extract(Connection conn, Instrument instrument, DataSet dataSet)
    throws Exception;

  /**
   * Retrieve the source {@link DataFiles} which are used in the
   * {@link DataSet}.
   *
   * @return The used files.
   */
  public Set<DataFile> getUsedFiles();

  /**
   * Get the extracted {@link SensorValue}s.
   *
   * @return The sensor values.
   */
  public NewSensorValues getSensorValues();

  /**
   * Get the geographical bounds of the dataset.
   *
   * <p>
   * May be null if the dataset contains no geographical data.
   * </p>
   *
   * @return The bounds.
   */
  public GeoBounds getGeoBounds();
}
