package uk.ac.exeter.QuinCe.jobs.files;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.exeter.QuinCe.User.User;
import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.DataSetDB;
import uk.ac.exeter.QuinCe.data.Dataset.DataSetDataDB;
import uk.ac.exeter.QuinCe.data.Dataset.InvalidDataSetStatusException;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.NewSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.RunTypePeriod;
import uk.ac.exeter.QuinCe.data.Dataset.RunTypePeriods;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValue;
import uk.ac.exeter.QuinCe.data.Dataset.TimeCoordinate;
import uk.ac.exeter.QuinCe.data.Dataset.TimeDataSet;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Files.DataFile;
import uk.ac.exeter.QuinCe.data.Files.DataFileDB;
import uk.ac.exeter.QuinCe.data.Files.TimeDataFile;
import uk.ac.exeter.QuinCe.data.Instrument.FileDefinition;
import uk.ac.exeter.QuinCe.data.Instrument.FileDefinitionException;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.MissingRunTypeException;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.Calibration;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.SensorCalibrationDB;
import uk.ac.exeter.QuinCe.data.Instrument.DataFormats.PositionException;
import uk.ac.exeter.QuinCe.data.Instrument.RunTypes.RunTypeAssignment;
import uk.ac.exeter.QuinCe.data.Instrument.RunTypes.RunTypeCategory;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorAssignment;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.jobs.InvalidJobParametersException;
import uk.ac.exeter.QuinCe.jobs.JobFailedException;
import uk.ac.exeter.QuinCe.jobs.JobThread;
import uk.ac.exeter.QuinCe.jobs.NextJobInfo;
import uk.ac.exeter.QuinCe.utils.DatabaseException;
import uk.ac.exeter.QuinCe.utils.DatabaseUtils;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.ExceptionUtils;
import uk.ac.exeter.QuinCe.utils.MissingParamException;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;
import uk.ac.exeter.QuinCe.utils.TimeRange;
import uk.ac.exeter.QuinCe.utils.TimeRangeBuilder;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Job to extract the data for a data set from the uploaded data files
 */
public class ExtractDataSetJob extends DataSetJob {

  /**
   * Name of the job, used for reporting
   */
  private final String jobName = "Dataset Extraction";

  /**
   * Initialise the job object so it is ready to run
   *
   * @param resourceManager
   *          The system resource manager
   * @param config
   *          The application configuration
   * @param jobId
   *          The id of the job in the database
   * @param parameters
   *          The job parameters, containing the file ID
   * @throws InvalidJobParametersException
   *           If the parameters are not valid for the job
   * @throws MissingParamException
   *           If any of the parameters are invalid
   * @throws RecordNotFoundException
   *           If the job record cannot be found in the database
   * @throws DatabaseException
   *           If a database error occurs
   */
  public ExtractDataSetJob(ResourceManager resourceManager, Properties config,
    long jobId, User owner, Properties properties) throws MissingParamException,
    InvalidJobParametersException, DatabaseException, RecordNotFoundException {
    super(resourceManager, config, jobId, owner, properties);
  }

  @Override
  protected NextJobInfo execute(JobThread thread) throws JobFailedException {

    Connection conn = null;

    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);

      // Clear any existing data for the DataSet
      resetDataset(conn);
      conn.commit();

      // Get the new data set from the database
      TimeDataSet dataSet = (TimeDataSet) getDataset(conn);
      dataSet.setStatus(DataSet.STATUS_DATA_EXTRACTION);
      DataSetDB.updateDataSet(conn, dataSet);
      conn.commit();

      Instrument instrument = getInstrument(conn);

      // If the new dataset overlaps the NRT dataset, mark it for deletion.
      // It will get removed and recreated by the NRT scripts outside QuinCe
      TimeDataSet nrtDataset = (TimeDataSet) DataSetDB.getNrtDataSet(conn,
        dataSet.getInstrumentId());
      if (null != nrtDataset && TimeDataSet.overlap(nrtDataset, dataSet)) {
        DataSetDB.setNrtDatasetStatus(dataSource, instrument,
          DataSet.STATUS_REPROCESS);
      }

      List<TimeDataFile> allFiles = new ArrayList<DataFile>(
        DataFileDB.getFiles(conn, instrument)).stream()
        .map(f -> (TimeDataFile) f).toList();

      List<TimeDataFile> potentialFiles = TimeDataFile.filter(allFiles,
        dataSet.getStartTime(), dataSet.getEndTime(), true);

      Set<DataFile> usedFiles = new HashSet<DataFile>(potentialFiles.size());

      NewSensorValues sensorValues = new NewSensorValues(dataSet);

      // We want to store when run types begin and end
      RunTypePeriods runTypePeriods = new RunTypePeriods();

      CalibrationSet sensorCalibrations = SensorCalibrationDB.getInstance()
        .getCalibrationSet(conn, dataSet);

      // Adjust the DataSet bounds to the latest start date and earliest end
      // date of each file definition, if the dataset range is beyond them
      Map<FileDefinition, TimeRangeBuilder> fileDefinitionRanges = new HashMap<FileDefinition, TimeRangeBuilder>();
      instrument.getFileDefinitions()
        .forEach(fd -> fileDefinitionRanges.put(fd, new TimeRangeBuilder()));

      for (DataFile file : potentialFiles) {
        if (!(file instanceof TimeDataFile)) {
          throw new IllegalArgumentException("File of wrong type");
        }

        TimeDataFile castFile = (TimeDataFile) file;

        fileDefinitionRanges.get(castFile.getFileDefinition()).add(castFile);
      }

      LocalDateTime filesLatestStart = TimeRange
        .getLatestStart(fileDefinitionRanges.values());
      if (filesLatestStart.isAfter(dataSet.getStartTime())) {
        dataSet.setStartTime(filesLatestStart);
      }

      LocalDateTime filesEarliestEnd = TimeRange
        .getEarliestEnd(fileDefinitionRanges.values());
      if (filesEarliestEnd.isBefore(dataSet.getEndTime())) {
        dataSet.setEndTime(filesEarliestEnd);
      }

      // Collect the data bounds
      double minLon = Double.MAX_VALUE;
      double maxLon = -Double.MAX_VALUE;
      double minLat = Double.MAX_VALUE;
      double maxLat = -Double.MAX_VALUE;

      for (DataFile file : potentialFiles) {
        FileDefinition fileDefinition = file.getFileDefinition();
        int currentLine = file.getFirstDataLine();
        while (currentLine < file.getContentLineCount()) {

          try {
            List<String> line = file.getLine(currentLine);

            // Check the number of columns on the line
            boolean checkColumnCount = true;

            if (fileDefinition.hasRunTypes()) {
              try {
                RunTypeCategory runType = null;

                RunTypeAssignment runTypeAssignment = fileDefinition
                  .getRunType(line, true);

                if (null != runTypeAssignment) {
                  runType = runTypeAssignment.getCategory();
                }

                if (null != runType
                  && runType.equals(RunTypeCategory.IGNORED)) {
                  checkColumnCount = false;
                }
              } catch (FileDefinitionException e) {
                dataSet.addProcessingMessage(jobName, file, currentLine, e);
                if (e instanceof MissingRunTypeException) {
                  dataSet.addProcessingMessage(jobName, file, currentLine,
                    "Unrecognised Run Type");
                }
              }
            }

            if (checkColumnCount
              && line.size() != fileDefinition.getColumnCount()) {
              dataSet.addProcessingMessage(jobName, file, currentLine,
                "Incorrect number of columns");
            }

            LocalDateTime time = ((TimeDataFile) file).getOffsetTime(line);

            if ((time.equals(dataSet.getStartTime())
              || time.isAfter(dataSet.getStartTime()))
              && (time.isBefore(dataSet.getEndTime())
                || time.isEqual(dataSet.getEndTime()))) {

              // We're using this file
              usedFiles.add(file);

              if (!dataSet.fixedPosition() && fileDefinition.hasPosition()) {

                String longitude = null;
                try {
                  longitude = file.getLongitude(line);
                } catch (PositionException e) {
                  dataSet.addProcessingMessage(jobName, file, currentLine, e);
                }

                if (null != longitude) {
                  sensorValues.create(FileDefinition.LONGITUDE_COLUMN_ID, time,
                    longitude);

                  // Update the dataset bounds
                  try {
                    double lonDouble = Double.parseDouble(longitude);
                    if (lonDouble < minLon) {
                      minLon = lonDouble;
                    }

                    if (lonDouble > maxLon) {
                      maxLon = lonDouble;
                    }
                  } catch (NumberFormatException e) {
                    // Ignore it now. QC will pick it up later.
                  }
                }

                String latitude = null;
                try {
                  latitude = file.getLatitude(line);
                } catch (PositionException e) {
                  dataSet.addProcessingMessage(jobName, file, currentLine, e);
                }

                if (null != latitude) {
                  sensorValues.create(FileDefinition.LATITUDE_COLUMN_ID, time,
                    latitude);

                  // Update the dataset bounds
                  try {
                    double latDouble = Double.parseDouble(latitude);
                    if (latDouble < minLat) {
                      minLat = latDouble;
                    }

                    if (latDouble > maxLat) {
                      maxLat = latDouble;
                    }
                  } catch (NumberFormatException e) {
                    // Ignore it now. QC will pick it up later.
                  }
                }
              }

              // Assigned columns
              for (Entry<SensorType, TreeSet<SensorAssignment>> entry : instrument
                .getSensorAssignments().entrySet()) {

                for (SensorAssignment assignment : entry.getValue()) {
                  if (assignment.getDataFile()
                    .equals(fileDefinition.getFileDescription())) {

                    // For run types, follow all aliases
                    if (entry.getKey()
                      .equals(SensorType.RUN_TYPE_SENSOR_TYPE)) {

                      RunTypeAssignment runTypeValue = file.getFileDefinition()
                        .getRunType(line, true);

                      if (null != runTypeValue) {
                        String runType = runTypeValue.getRunName();

                        sensorValues.create(assignment.getDatabaseId(), time,
                          runType);

                        runTypePeriods.add(runType, time);
                      }
                    } else {

                      // Create the SensorValue object
                      String fieldValue = null;

                      fieldValue = file.getStringValue(jobName, dataSet,
                        currentLine, line, assignment.getColumn(),
                        assignment.getMissingValue());

                      if (null != fieldValue) {
                        SensorValue sensorValue = sensorValues
                          .create(assignment.getDatabaseId(), time, fieldValue);

                        // Apply calibration if required
                        Calibration sensorCalibration = sensorCalibrations
                          .getCalibrations(time)
                          .get(String.valueOf(assignment.getDatabaseId()));

                        if (null != sensorCalibration) {
                          sensorValue.calibrateValue(sensorCalibration);
                        }
                      }
                    }
                  }
                }
              }
            }
          } catch (Throwable e) {
            // Log the error but continue with the next line
            dataSet.addProcessingMessage(jobName, file, currentLine, e);
          }

          currentLine++;
        }
      }

      // The last run type will cover the rest of time
      runTypePeriods.finish();

      // Now flag all the values that have internal calibrations and are within
      // the instrument's pre- and post-flushing periods (if they're defined),
      // or are in an INGORED run type
      if (runTypePeriods.size() > 0) {
        RunTypePeriod currentPeriod = runTypePeriods.get(0);
        int currentPeriodIndex = 0;

        Iterator<SensorValue> valuesIter = sensorValues.iterator();
        while (valuesIter.hasNext()) {
          SensorValue value = valuesIter.next();
          SensorType sensorType = instrument.getSensorAssignments()
            .getSensorTypeForDBColumn(value.getColumnId());

          if (sensorType.hasInternalCalibration()) {
            boolean periodFound = false;

            // Make sure we have the correct run type period
            while (!periodFound) {

              // If we have multiple file definitions, it's possible that
              // timestamps in the file where the run type *isn't* defined will
              // fall between run types.
              //
              // In this case, simply use the next known run type. Otherwise we
              // find the run type that the timestamp is in.
              if (((TimeCoordinate) value.getCoordinate())
                .isBefore(currentPeriod.getStart())
                || currentPeriod.encompasses(
                  ((TimeCoordinate) value.getCoordinate()).getTime())) {
                periodFound = true;
              } else {
                currentPeriodIndex++;
                currentPeriod = runTypePeriods.get(currentPeriodIndex);
              }
            }

            // If the current period is an IGNORE run type, remove the value.
            // We can only tell this for "Generic" instruments, ie those with a
            // Run Type column
            if (instrument
              .getRunTypeCategory(Measurement.RUN_TYPE_DEFINES_VARIABLE,
                currentPeriod.getRunType())
              .equals(RunTypeCategory.IGNORED)) {
              valuesIter.remove();
            } else if (inFlushingPeriod(value.getCoordinate(), currentPeriod,
              instrument)) {

              // Flag flushing values
              value.setUserQC(Flag.FLUSHING, "");
            }
          }
        }
      }

      // Store the remaining values
      if (sensorValues.size() > 0) {
        DataSetDataDB.storeNewSensorValues(conn, sensorValues);
      }

      conn.commit();
      conn.setAutoCommit(true);

      dataSet.setBounds(minLon, minLat, maxLon, maxLat);

      // Store the used files
      DataSetDB.storeDatasetFiles(conn, dataSet, usedFiles);

      // Trigger the Auto QC job
      dataSet.setStatus(DataSet.STATUS_SENSOR_QC);
      DataSetDB.updateDataSet(conn, dataSet);

      Properties jobProperties = new Properties();
      jobProperties.setProperty(DataSetJob.ID_PARAM, String
        .valueOf(Long.parseLong(properties.getProperty(DataSetJob.ID_PARAM))));
      NextJobInfo nextJob = new NextJobInfo(AutoQCJob.class.getCanonicalName(),
        jobProperties);
      nextJob.putTransferData(SENSOR_VALUES, sensorValues.toSet());
      return nextJob;
    } catch (Exception e) {
      ExceptionUtils.printStackTrace(e);
      DatabaseUtils.rollBack(conn);
      try {
        // Set the dataset to Error status
        getDataset(conn).setStatus(DataSet.STATUS_ERROR);
        // And add a (friendly) message...
        StringBuffer message = new StringBuffer();
        message.append(getJobName());
        message.append(" - error: ");
        message.append(e.getMessage());
        getDataset(conn).addMessage(message.toString(),
          ExceptionUtils.getStackTrace(e));
        DataSetDB.updateDataSet(conn, getDataset(conn));
        conn.commit();
      } catch (Exception e1) {
        ExceptionUtils.printStackTrace(e1);
      }
      throw new JobFailedException(id, e);
    } finally {
      try {
        if (!conn.getAutoCommit()) {
          conn.setAutoCommit(true);
        }
      } catch (SQLException e) {
        // NOOP
      }
      DatabaseUtils.closeConnection(conn);
    }
  }

  private boolean inFlushingPeriod(Coordinate coordinate,
    RunTypePeriod runTypePeriod, Instrument instrument) {

    boolean result = false;

    Integer preFlushingTime = instrument
      .getIntProperty(Instrument.PROP_PRE_FLUSHING_TIME);
    Integer postFlushingTime = instrument
      .getIntProperty(Instrument.PROP_POST_FLUSHING_TIME);

    if (null != preFlushingTime && preFlushingTime > 0
      && DateTimeUtils.secondsBetween(runTypePeriod.getStart(),
        coordinate.getTime()) <= preFlushingTime) {
      result = true;
    } else if (null != postFlushingTime && postFlushingTime > 0
      && DateTimeUtils.secondsBetween(coordinate.getTime(),
        runTypePeriod.getEnd()) <= postFlushingTime) {
      result = true;
    }

    return result;
  }

  @Override
  public String getJobName() {
    return jobName;
  }

  /**
   * Reset the data set processing.
   *
   * Delete all related records and reset the status
   *
   * @throws MissingParamException
   *           If any of the parameters are invalid
   * @throws InvalidDataSetStatusException
   *           If the method sets an invalid data set status
   * @throws DatabaseException
   *           If a database error occurs
   * @throws RecordNotFoundException
   *           If the record don't exist
   */
  protected void resetDataset(Connection conn) throws JobFailedException {

    try {
      long datasetId = getDataset(conn).getId();
      DataSetDataDB.deleteDataReduction(conn, datasetId);
      DataSetDataDB.deleteMeasurements(conn, datasetId);
      DataSetDataDB.deleteSensorValues(conn, datasetId);
    } catch (Exception e) {
      throw new JobFailedException(id, "Error while resetting dataset", e);
    }
  }
}
