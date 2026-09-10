package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
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

  private static List<CalculationParameter> calculationParameters = null;

  public HaganGenXAirReducer(Variable variable,
    Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {
    // TODO Auto-generated method stub
    double zeroCalK = getZeroCalK(measurement);
    record.put("ZeroCalK", zeroCalK);

    // Get the spanCalK at the measurement time
    double spanCalK = getSpanCalK(measurement);
    record.put("SpanCalK", spanCalK);

  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(1);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "ZeroCalK", "ZeroCalK", "ZeroCalK", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "SpanCalK", "SpanCalK", "SpanCalK", "", true));
    }

    return calculationParameters;
  }
}
