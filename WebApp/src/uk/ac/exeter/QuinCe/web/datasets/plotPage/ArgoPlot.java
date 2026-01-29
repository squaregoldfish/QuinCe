package uk.ac.exeter.QuinCe.web.datasets.plotPage;

public class ArgoPlot extends Plot {

  public ArgoPlot(PlotPageData data, PlotPageColumnHeading xAxis,
    PlotPageColumnHeading yAxis, boolean useNeededFlags) throws Exception {
    super(data, xAxis, yAxis, useNeededFlags);
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
}
