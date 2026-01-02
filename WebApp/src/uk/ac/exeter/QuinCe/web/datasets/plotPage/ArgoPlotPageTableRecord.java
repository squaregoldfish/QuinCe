package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import uk.ac.exeter.QuinCe.data.Dataset.ArgoCoordinate;
import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;

public class ArgoPlotPageTableRecord extends PlotPageTableRecord {

  public ArgoPlotPageTableRecord(long id) {
    super(id);
  }

  public ArgoPlotPageTableRecord(ArgoCoordinate id) {
    super(id);
  }

  @Override
  public void addCoordinate(Coordinate coordinate) {
    ArgoCoordinate castCoordinate = (ArgoCoordinate) coordinate;
    addColumn(
      new SimplePlotPageTableValue(String.valueOf(castCoordinate.getNLevel()),
        Flag.GOOD, "", false, PlotPageTableValue.MEASURED_TYPE, -1L));
    addColumn(
      new SimplePlotPageTableValue(String.valueOf(castCoordinate.getPres()),
        Flag.GOOD, "", false, PlotPageTableValue.MEASURED_TYPE, -1L));
  }
}
