package uk.ac.exeter.QuinCe.web.datasets.plotPage.ManualQC;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javadocmd.simplelatlng.LatLng;

import uk.ac.exeter.QuinCe.data.Dataset.ArgoCoordinate;
import uk.ac.exeter.QuinCe.data.Dataset.ArgoProfile;
import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValue;
import uk.ac.exeter.QuinCe.data.Dataset.DataReduction.ReadOnlyDataReductionRecord;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorsConfiguration;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.ArgoPlotPageTableRecord;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.CoordinateIdSerializer;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.MapRecords;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageColumnHeading;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageTableRecord;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageTableRecordSerializer;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageTableValue;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageValueMapRecord;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

public class ArgoManualQCData extends ManualQCData {

  /**
   * A Gson object.
   */
  private static Gson gson = new Gson();

  /**
   * Column heading for the Cycle Number, which is the basis for the map scale.
   */
  private PlotPageColumnHeading cycleNumberHeading;

  /**
   * The profiles which are the basis for user selection of the data view.
   */
  private List<ArgoProfile> profiles = null;

  /**
   * The contents of the Profiles table as a JSON String.
   */
  private String profileTableData = null;

  private int profileRecordCount = -1;

  /**
   * Construct the data object.
   *
   * <p>
   * Initially the object is empty. The data will be loaded by the
   * {@link #load(DataSource)} method.
   * </p>
   *
   * @param instrument
   *          The instrument that the dataset belongs to.
   * @param dataset
   *          The dataset.
   * @param dataSource
   *          A data source.
   * @throws SQLException
   * @throws Exception
   *           If the data cannot be loaded.
   */
  protected ArgoManualQCData(DataSource dataSource, Instrument instrument,
    DataSet dataset) throws SensorTypeNotFoundException, SQLException {
    super(dataSource, instrument, dataset);

    SensorType cycleNumberSensorType = ResourceManager.getInstance()
      .getSensorsConfiguration().getSensorType("Cycle Number");

    cycleNumberHeading = new PlotPageColumnHeading(
      instrument.getSensorAssignments().get(cycleNumberSensorType).first()
        .getColumnHeading(),
      true, false, false);
  }

  @Override
  protected PlotPageColumnHeading getDefaultMap1Column() throws Exception {
    return cycleNumberHeading;
  }

  @Override
  protected List<PlotPageColumnHeading> buildRootColumns()
    throws SensorTypeNotFoundException {
    List<PlotPageColumnHeading> rootColumns = new ArrayList<PlotPageColumnHeading>(
      7);

    SensorsConfiguration sensorConfig = ResourceManager.getInstance()
      .getSensorsConfiguration();

    /*
     * We don't include the cycle number, direction or profile because they are
     * specified as part of the selected profile.
     *
     * Source File and Time are shown in the Profile Details and not as part of
     * the table.
     */
    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Level"), true, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Pressure (Depth)"), true, false, false));

    return rootColumns;
  }

  @Override
  protected List<PlotPageColumnHeading> buildExtendedRootColumns()
    throws SensorTypeNotFoundException {

    return buildRootColumns();
  }

  public String getProfileTableColumns() {
    return gson.toJson(Arrays.asList(
      new String[] { "index", "Cycle Number", "Direction", "Profile" }));
  }

  public String getProfileTableData() {
    if (null != sensorValues && null == profileTableData) {

      profiles = getCoordinates().stream().map(ArgoCoordinate.class::cast)
        .map(c -> c.toProfile()).distinct().toList();

      List<List<String>> profileData = new ArrayList<List<String>>(
        profiles.size());
      IntStream.range(0, profiles.size()).forEach(i -> {

        ArgoProfile profile = profiles.get(i);

        profileData.add(Arrays.asList(new String[] { String.valueOf(i),
          String.valueOf(profile.cycleNumber()),
          String.valueOf(profile.direction()),
          String.valueOf(profile.profile()) }));
      });

      profileTableData = gson.toJson(profileData);
    }

    return profileTableData;
  }

  /**
   * Generate the main table data for a specified {@link ArgoProfile}.
   *
   * <p>
   * This is used in place of the normal implementation of
   * {@link ManualQCData#generateTableData(int, int). The method always loads
   * the complete set of data for the currently selected profile, rather than
   * loading a portion of the complete dataset's data.
   * </p>
   *
   * <p>
   * The format of this method's output is the same as for that method.
   * </p>
   */
  public String generateTableData(ArgoProfile selectedProfile) {

    // Get the Coordinates matching the profile
    List<Coordinate> cycleCoordinates = getCoordinates().stream()
      .filter(c -> selectedProfile.matches((ArgoCoordinate) c)).toList();

    profileRecordCount = cycleCoordinates.size();

    List<PlotPageTableRecord> records = new ArrayList<PlotPageTableRecord>(
      cycleCoordinates.size());

    try {
      for (Coordinate baseCoordinate : cycleCoordinates) {
        ArgoCoordinate coordinate = (ArgoCoordinate) baseCoordinate;

        PlotPageTableRecord record = new ArgoPlotPageTableRecord(coordinate);
        record.addCoordinate(coordinate);

        Map<Long, SensorValue> recordSensorValues = sensorValues
          .get(coordinate);

        Long measurementId = null;
        Measurement measurement = measurements.get(coordinate);
        if (null != measurement) {
          measurementId = measurement.getId();
        }

        Map<Variable, ReadOnlyDataReductionRecord> dataReductionData = null;

        if (null != measurementId) {
          // Retrieve the data reduction data
          dataReductionData = dataReduction.get(measurementId);
        }

        for (long columnId : sensorColumnIds) {
          record.addColumn(recordSensorValues.get(columnId));
        }

        addDiagnosticColumns(record, recordSensorValues);
        addMeasurementColumns(record, measurement);
        addDataReductionColumns(record, dataReductionData);

        records.add(record);
      }

    } catch (Exception e) {
      error("Error building table data", e);
    }

    return tableDataGson.toJson(records);
  }

  @Override
  public int size() {
    return profileRecordCount;
  }

  /**
   * Build map records based on cycle numbers, with one record per cycle.
   */
  @Override
  protected void buildMapCache(PlotPageColumnHeading column) throws Exception {

    MapRecords records = new MapRecords(0, getAllSensorValues());

    TreeMap<Coordinate, PlotPageTableValue> values = getColumnValues(column);
    HashSet<Integer> usedCycleNumbers = new HashSet<Integer>();

    for (Map.Entry<Coordinate, PlotPageTableValue> entry : values.entrySet()) {
      ArgoCoordinate coordinate = (ArgoCoordinate) entry.getKey();

      if (!usedCycleNumbers.contains(coordinate.getCycleNumber())) {
        LatLng position = getMapPosition(entry.getKey());
        if (null != position) {
          records.add(
            new PlotPageValueMapRecord(position, coordinate, entry.getValue()));
          usedCycleNumbers.add(coordinate.getCycleNumber());
        }
      }
    }

    mapCache.put(column, records);
  }

  @Override
  protected void initTableDataGson() {
    tableDataGson = new GsonBuilder()
      .registerTypeHierarchyAdapter(PlotPageTableRecord.class,
        new PlotPageTableRecordSerializer(getAllSensorValues()))
      .registerTypeHierarchyAdapter(Coordinate.class,
        new CoordinateIdSerializer())
      .create();
  }
}
