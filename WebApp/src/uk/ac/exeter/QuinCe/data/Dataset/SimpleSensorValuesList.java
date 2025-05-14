package uk.ac.exeter.QuinCe.data.Dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.ac.exeter.QuinCe.data.Dataset.QC.RoutineException;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;

/**
 * A basic implementation of {@link SensorValuesList} where {@link SensorValues}
 * are returned exactly as they are added, with no extra processing.s
 */
public class SimpleSensorValuesList extends SensorValuesList {

  private List<SingleSensorValuesListValue> outputValues = null;

  protected SimpleSensorValuesList(long columnId,
    DatasetSensorValues allSensorValues, boolean forceString)
    throws RecordNotFoundException {
    super(columnId, allSensorValues, forceString);
  }

  protected SimpleSensorValuesList(Collection<Long> columnIds,
    DatasetSensorValues allSensorValues, boolean forceString)
    throws RecordNotFoundException {
    super(columnIds, allSensorValues, forceString);
  }

  @Override
  public SensorValuesListValue getValue(Coordinate coordinate,
    boolean interpolate) throws SensorValuesListException {

    int index = Collections.binarySearch(getOutputCoordinates(), coordinate);
    return index >= 0 ? getOutputValues().get(index) : null;
  }

  private void buildOutputValues() throws SensorValuesListException {

    outputValues = new ArrayList<SingleSensorValuesListValue>(list.size());

    for (SensorValue value : list) {
      try {
        outputValues.add(
          new SingleSensorValuesListValue(value, sensorType, allSensorValues));
      } catch (RoutineException e) {
        throw new SensorValuesListException(e);
      }
    }
  }

  @Override
  protected List<? extends SensorValuesListValue> getOutputValues()
    throws SensorValuesListException {
    if (null == outputValues) {
      buildOutputValues();
    }

    return outputValues;
  }

  @Override
  protected void listContentsUpdated() {
    outputValues = null;
  }
}
