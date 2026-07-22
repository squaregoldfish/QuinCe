package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import org.apache.commons.lang3.NotImplementedException;

import com.javadocmd.simplelatlng.LatLng;

import uk.ac.exeter.QuinCe.data.Dataset.Coordinate;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Dataset.QC.FlagScheme;
import uk.ac.exeter.QuinCe.utils.StringUtils;

public class PlotPageValueMapRecord extends MapRecord {

  private final PlotPageTableValue value;

  private final DatasetSensorValues allSensorValues;

  public PlotPageValueMapRecord(LatLng position, Coordinate coordinate,
    PlotPageTableValue value, DatasetSensorValues allSensorValues) {
    super(coordinate, position, coordinate.getId());
    this.value = value;
    this.allSensorValues = allSensorValues;
  }

  public PlotPageValueMapRecord(Coordinate coordinate, LatLng position, long id,
    PlotPageTableValue value, DatasetSensorValues allSensorValues) {
    super(coordinate, position, id);
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

    if (null != value) {
      result = StringUtils.doubleFromString(value.getValue(allSensorValues));
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

  @Override
  public Coordinate getCoordinate() {
    if (null == coordinate) {
      throw new NotImplementedException("Coordinates not available");
    } else {
      return super.getCoordinate();
    }
  }

}
