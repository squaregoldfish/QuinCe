package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;

/**
 * Reducer for Hagan GenX air measurements.
 *
 * <p>
 * The logic for this is mostly identical as for the water measurements, so this
 * is an extension of that reducer.
 * </p>
 *
 * @see HaganGenXEqReducer
 */
public class HaganGenXAirReducer extends HaganGenXEqReducer {

  public HaganGenXAirReducer(Variable variable,
    Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

}
