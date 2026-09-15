package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.mutable.MutableDouble;

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

    double zeroCalK = getZeroCalK(measurement);
    record.put("ZeroCalK_air", zeroCalK);

    // Get the spanCalK at the measurement time
    double spanCalK = getSpanCalK(measurement);
    record.put("SpanCalK_air", spanCalK);

    Double temp = measurement.getMeasurementValue(tempSensorType)
      .getCalculatedValue();
    Double pressure = measurement.getMeasurementValue(pressureSensorType)
      .getCalculatedValue();
    Double co2Raw1 = measurement.getMeasurementValue(co2Raw1SensorType)
      .getCalculatedValue();
    Double co2Raw2 = measurement.getMeasurementValue(co2Raw2SensorType)
      .getCalculatedValue();
    Double rh = measurement.getMeasurementValue(rhSensorType)
      .getCalculatedValue();
    Double rhTemp = measurement.getMeasurementValue(rhTempSensorType)
      .getCalculatedValue();
    Double spanSlope = measurement.getMeasurementValue(spanSlopeSensorType)
      .getCalculatedValue();

    // Can these persist across measurements?
    MutableDouble r_absp = new MutableDouble(0D);
    MutableDouble s_absp = new MutableDouble(0D);

    double xCO2Wet = calculatedCO2(temp, pressure, co2Raw1, co2Raw2, rh, rhTemp,
      zeroCalK, spanCalK, r_absp, s_absp, spanSlope);

    record.put("xCO2Wet_air", xCO2Wet);

    double spanRh = getSpanRh(measurement);
    double spanRhTemp = getSpanRhTemp(measurement);
    record.put("spanRh_air", spanRh);
    record.put("spanRhTemp_air", spanRhTemp);

    double vpSat = 0.61365484
      * Math.exp(17.502 * spanRhTemp / (240.97 + spanRhTemp));

    double co2VPrh = ((rh - spanRh) * vpSat) / 100;
    double xCO2Dry = xCO2Wet * pressure / (pressure - co2VPrh);

    record.put("co2VPrh_air", co2VPrh);
    record.put("xCO2Dry_air", xCO2Dry);
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(1);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "ZeroCalK_air", "ZeroCalK_air", "ZeroCalK_air", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "SpanCalK_air", "SpanCalK_air", "SpanCalK_air", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "xCO2Wet_air", "xCO2Wet_air", "xCO2Wet", "", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "spanRh_air", "spanRh_air", "spanRh_air", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "spanRhTemp_air", "spanRhTemp_air", "spanRhTemp_air", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "co2VPrh_air", "co2VPrh_air", "co2VPrh_air", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(6),
        "xCO2Dry_air", "xCO2Dry_air", "xCO2Dry_air", "", true));
    }

    return calculationParameters;
  }
}
