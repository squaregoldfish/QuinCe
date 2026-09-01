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
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

public class DExcessReducer extends DataReducer {

  private static List<CalculationParameter> calculationParameters = null;

  public DExcessReducer(Variable variable, Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {

    try {
      DoubleWithUncertainty dH218O = measurement.getMeasurementValue("δH₂¹⁸O")
        .getCalculatedValue();
      DoubleWithUncertainty dHD16O = measurement.getMeasurementValue("δHD¹⁶O")
        .getCalculatedValue();

      DoubleWithUncertainty dExcess = dHD16O.subtract(dH218O.multiply(8));

      record.put("D-Excess", dExcess);
    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(1);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "D-Excess", "D-Excess", "DEXCESS", "permil", true));
    }

    return calculationParameters;
  }
}
