package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import uk.ac.exeter.QuinCe.data.Dataset.ArgoCoordinate;
import uk.ac.exeter.QuinCe.data.Dataset.ArgoProfile;
import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.web.datasets.plotPage.ManualQC.ArgoManualQCData;

public class ArgoPlot extends Plot {

  /**
   * Stores the data object locally as the correct class.
   */
  ArgoManualQCData data;

  public ArgoPlot(ArgoManualQCData data, PlotPageColumnHeading xAxis,
    PlotPageColumnHeading yAxis, boolean useNeededFlags) throws Exception {
    super(data, xAxis, yAxis, useNeededFlags);

    this.data = data;
  }

  @Override
  public void setYaxis(long yAxis) throws Exception {
    // We ignore any requests to change Y axis
  }

  @Override
  public void setY2axis(long y2Axis) throws Exception {
    // We ignore any requests to change Y2 axis
  }

  @Override
  protected Double scaleYValue(Double yValue) {
    // Depths are displayed as negative in plots.
    return yValue * -1D;
  }

  private TreeMap<Coordinate, PlotPageTableValue> getColumnValues(
    PlotPageColumnHeading column, ArgoProfile profile) throws Exception {

    TreeMap<Coordinate, PlotPageTableValue> allValues = data
      .getColumnValues(column);

    return allValues.entrySet().stream()
      .filter(e -> profile.matches((ArgoCoordinate) e.getKey()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
        (e1, e2) -> e1, TreeMap::new));
  }

  @Override
  protected TreeMap<Coordinate, PlotPageTableValue> getXValues()
    throws Exception {

    ArgoProfile profile = data.getProfiles().get(data.getSelectedProfile());
    return getColumnValues(xAxis, profile);
  }

  @Override
  protected TreeMap<Coordinate, PlotPageTableValue> getYValues()
    throws Exception {

    ArgoProfile profile = data.getProfiles().get(data.getSelectedProfile());
    return getColumnValues(yAxis, profile);
  }

  protected TreeMap<Coordinate, PlotPageTableValue> getY2Values()
    throws Exception {

    ArgoProfile profile = data.getProfiles().get(data.getSelectedProfile());
    return null == y2Axis ? new TreeMap<>() : getColumnValues(y2Axis, profile);
  }
}
