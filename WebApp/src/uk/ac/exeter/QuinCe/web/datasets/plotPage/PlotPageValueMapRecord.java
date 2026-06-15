package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import com.javadocmd.simplelatlng.LatLng;

import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Dataset.QC.FlagScheme;

public class PlotPageValueMapRecord extends MapRecord {

  private final PlotPageTableValue value;

  private final DatasetSensorValues allSensorValues;

  public PlotPageValueMapRecord(LatLng position, Coordinate coordinate,
    PlotPageTableValue value, DatasetSensorValues allSensorValues) {
    super(position, coordinate.getId());
    this.value = value;
    this.allSensorValues = allSensorValues;
  }

  public PlotPageValueMapRecord(LatLng position, long id,
    PlotPageTableValue value, DatasetSensorValues allSensorValues) {
    super(position, id);
    this.value = value;
    this.allSensorValues = allSensorValues;
  }

  @Override
  public boolean isGood(DatasetSensorValues allSensorValues) {
    return allSensorValues.getFlagScheme()
      .isGood(value.getQcFlag(allSensorValues), true);
  }

  @Override
  public boolean flagNeeded() {
    return value.getFlagNeeded();
  }

  @Override
  public Double getValue() {
    Double result = Double.NaN;

    if (null != value && null != value.getValue(allSensorValues)) {
      result = Double.parseDouble(value.getValue(allSensorValues));
    }

    return result;
  }

  @Override
  public Flag getFlag(DatasetSensorValues allSensorValues,
    boolean ignoreNeeded) {
    Flag result;

    if (!ignoreNeeded && flagNeeded()) {
      result = FlagScheme.NEEDED_FLAG;
    } else {
      result = value.getQcFlag(allSensorValues);
    }

    return result;
  }

}
