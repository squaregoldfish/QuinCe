package uk.ac.exeter.QuinCe.data.Dataset;

import java.util.Collection;

import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.VariablePropertiesException;
import uk.ac.exeter.QuinCe.utils.RecordNotFoundException;

public class SensorValuesListFactory {

  public static SensorValuesList makeSensorValuesList(long columnId,
    Instrument instrument, DatasetSensorValues allSensorValues,
    boolean forceString)
    throws RecordNotFoundException, VariablePropertiesException {

    switch (allSensorValues.getInstrument().getBasis()) {
    case Instrument.BASIS_TIME: {
      return new TimestampSensorValuesList(columnId, allSensorValues,
        instrument.getMeasurementMode(), forceString);
    }
    default: {
      return new SimpleSensorValuesList(columnId, allSensorValues, forceString);
    }
    }
  }

  public static SensorValuesList makeSensorValuesList(
    Collection<Long> columnIds, Instrument instrument,
    DatasetSensorValues allSensorValues, boolean forceString)
    throws RecordNotFoundException, VariablePropertiesException {

    switch (allSensorValues.getInstrument().getBasis()) {
    case Instrument.BASIS_TIME: {
      return new TimestampSensorValuesList(columnIds, allSensorValues,
        instrument.getMeasurementMode(), forceString);
    }
    default: {
      return new SimpleSensorValuesList(columnIds, allSensorValues,
        forceString);
    }
    }
  }

}
