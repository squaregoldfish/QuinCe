package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import com.javadocmd.simplelatlng.LatLng;

import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;

public class PlotPageValueMapRecord extends MapRecord {

  private PlotPageTableValue value;

  public PlotPageValueMapRecord(LatLng position, Coordinate coordinate,
    PlotPageTableValue value) {
    super(position, coordinate.getId());
    this.value = value;
  }

  public PlotPageValueMapRecord(LatLng position, long id,
    PlotPageTableValue value) {
    super(position, id);
    this.value = value;
  }

  @Override
  public boolean isGood(DatasetSensorValues allSensorValues) {
    return value.getQcFlag(allSensorValues).isGood();
  }

  @Override
  public boolean flagNeeded() {
    return value.getFlagNeeded();
  }

  @Override
  public Double getValue() {
    Double result = Double.NaN;

    if (null != value && null != value.getValue()) {
      result = Double.parseDouble(value.getValue());
    }

    return result;
  }

  @Override
  public Flag getFlag(DatasetSensorValues allSensorValues,
    boolean ignoreNeeded) {
    Flag result;

    if (!ignoreNeeded && flagNeeded()) {
      result = Flag.NEEDED;
    } else {
      result = value.getQcFlag(allSensorValues);
    }

    return result;
  }

}
