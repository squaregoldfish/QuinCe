package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;

/**
 * Reducer for Hagan GenX water measurements.
 */
public class HaganGenXEqReducer extends DataReducer {

  private static List<CalculationParameter> calculationParameters = null;

  public HaganGenXEqReducer(Variable variable,
    Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

  public void preprocess(Connection conn, Instrument instrument,
    DataSet dataset, List<Measurement> allMeasurements)
    throws DataReductionException {

    // PROCESS ALL THE ZEROS AND SPANS HERE

  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {
    // TODO Auto-generated method stub

    record.put("Dummy", 8D);
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(1);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "Dummy", "Dummy", "DUMMY", "dummy", true));
    }

    return calculationParameters;
  }

}
