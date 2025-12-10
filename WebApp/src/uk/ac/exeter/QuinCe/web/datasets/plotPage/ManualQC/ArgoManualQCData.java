package uk.ac.exeter.QuinCe.web.datasets.plotPage.ManualQC;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.google.gson.Gson;

import uk.ac.exeter.QuinCe.data.Dataset.ArgoCoordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Instrument.FileDefinition;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorsConfiguration;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.PlotPageColumnHeading;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

public class ArgoManualQCData extends ManualQCData {

  private static Gson gson = new Gson();

  private PlotPageColumnHeading cycleNumberHeading;

  private List<List<String>> profileTableData = null;

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

    rootColumns.add(cycleNumberHeading);

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Profile"), true, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Direction"), false, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Level"), true, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Pressure (Depth)"), true, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      sensorConfig.getSensorType("Source File"), false, false, false));

    rootColumns.add(new PlotPageColumnHeading(
      FileDefinition.TIME_COLUMN_HEADING, false, false, false));

    return rootColumns;
  }

  @Override
  protected List<PlotPageColumnHeading> buildExtendedRootColumns()
    throws SensorTypeNotFoundException {

    return buildRootColumns();
  }

  public String getProfileTableColumns() {
    return gson
      .toJson(Arrays.asList(new String[] { "Cycle Number", "Direction" }));
  }

  public String getProfileTableData() {
    if (null != sensorValues && null == profileTableData) {
      profileTableData = getCoordinates().stream()
        .map(ArgoCoordinate.class::cast)
        .map(
          c -> Arrays.asList(new String[] { String.valueOf(c.getCycleNumber()),
            String.valueOf(c.getDirection()) }))
        .distinct().toList();
    }

    return gson.toJson(profileTableData);
  }
}
