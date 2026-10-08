package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.data.Dataset.QC.FlagScheme;
import uk.ac.exeter.QuinCe.utils.DatabaseUtils;

/**
 * Stub {@link PlotPageTableValue} for a null value.
 */
public class NullPlotPageTableValue implements PlotPageTableValue {

  @Override
  public long getId() {
    return DatabaseUtils.NO_DATABASE_RECORD;
  }

  @Override
  public String getValue() {
    return "";
  }

  @Override
  public String getUncertainty() {
    return "";
  }

  @Override
  public Object getRawValue() {
    return "";
  }

  @Override
  public Flag getQcFlag(DatasetSensorValues allSensorValues) {
    return FlagScheme.NO_QC_FLAG;
  }

  @Override
  public String getQcMessage(DatasetSensorValues allSensorValues,
    boolean replaceNewlines) {
    return null;
  }

  @Override
  public boolean getFlagNeeded() {
    return false;
  }

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public char getType() {
    return PlotPageTableValue.MEASURED_TYPE;
  }

  @Override
  public Collection<Long> getSources() {
    return null;
  }
}
