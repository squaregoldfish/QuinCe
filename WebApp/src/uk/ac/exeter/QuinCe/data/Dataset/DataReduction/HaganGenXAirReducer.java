package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.MutableBigDecimal;

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

    BigDecimal zeroCalK = getZeroCalK(measurement);
    record.put("ZeroCalK_air", zeroCalK.doubleValue());

    // Get the spanCalK at the measurement time
    BigDecimal spanCalK = getSpanCalK(measurement);
    record.put("SpanCalK_air", spanCalK.doubleValue());

    BigDecimal temp = new BigDecimal(
      measurement.getMeasurementValue(tempSensorType).getCalculatedValue());
    BigDecimal pressure = new BigDecimal(
      measurement.getMeasurementValue(pressureSensorType).getCalculatedValue());
    BigDecimal co2Raw1 = new BigDecimal(
      measurement.getMeasurementValue(co2Raw1SensorType).getCalculatedValue());
    BigDecimal co2Raw2 = new BigDecimal(
      measurement.getMeasurementValue(co2Raw2SensorType).getCalculatedValue());
    BigDecimal rh = new BigDecimal(
      measurement.getMeasurementValue(rhSensorType).getCalculatedValue());
    BigDecimal rhTemp = new BigDecimal(
      measurement.getMeasurementValue(rhTempSensorType).getCalculatedValue());
    BigDecimal spanSlope = new BigDecimal(measurement
      .getMeasurementValue(spanSlopeSensorType).getCalculatedValue());

    // Can these persist across measurements?
    MutableBigDecimal r_absp = new MutableBigDecimal(BigDecimal.ZERO);
    MutableBigDecimal s_absp = new MutableBigDecimal(BigDecimal.ZERO);

    BigDecimal xCO2Wet = calculatedCO2(temp, pressure, co2Raw1, co2Raw2, rh,
      rhTemp, zeroCalK, spanCalK, r_absp, s_absp, spanSlope);

    record.put("xCO2Wet_air", xCO2Wet.doubleValue());

    BigDecimal spanRh = getSpanRh(measurement);
    BigDecimal spanRhTemp = getSpanRhTemp(measurement);
    record.put("spanRh_air", spanRh.doubleValue());
    record.put("spanRhTemp_air", spanRhTemp.doubleValue());

    BigDecimal vpSat = calcVpSat(spanRhTemp);
    BigDecimal co2VPrh = calcCo2VPrh(rh, spanRh, vpSat);
    BigDecimal xCO2Dry = calcXCO2Dry(xCO2Wet, pressure, co2VPrh);

    record.put("co2VPrh_air", co2VPrh.doubleValue());
    record.put("xCO2Dry_air", xCO2Dry.doubleValue());
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
