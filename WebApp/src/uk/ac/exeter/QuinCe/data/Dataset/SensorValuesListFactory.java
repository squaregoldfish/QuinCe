package uk.ac.exeter.QuinCe.data.Dataset;

import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;

public class SensorValuesListFactory {

  public static SensorValuesList makeSensorValuesList(long columnId,
    DatasetSensorValues allSensorValues) throws RecordNotFoundException {

    switch (allSensorValues.getInstrument().getBasis()) {
    case Instrument.BASIS_TIME: {
      return new TimestampSensorValuesList(columnId, allSensorValues);
    }
    default: {
      return new SensorValuesList(columnId, allSensorValues);
    }
    }
  }

  public static SensorValuesList makeSensorValuesList(
    Collection<Long> columnIds, DatasetSensorValues allSensorValues)
    throws RecordNotFoundException {

    switch (allSensorValues.getInstrument().getBasis()) {
    case Instrument.BASIS_TIME: {
      return new TimestampSensorValuesList(columnIds, allSensorValues);
    }
    default: {
      return new SensorValuesList(columnIds, allSensorValues);
    }
    }
  }

}
