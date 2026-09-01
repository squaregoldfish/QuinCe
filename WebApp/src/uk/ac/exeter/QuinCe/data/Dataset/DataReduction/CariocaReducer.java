package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.TimeDataSet;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalculationCoefficient;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.BigDecimalWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

/**
 * Data Reducer for Carioca CO2 sensor.
 *
 * <p>
 * The calculations are taken from a calibration sheet I was sent. We really
 * need a proper reference for it. In the meantime, the calibration sheet is in
 * the Github repository at {@code Documentation/Calculations/Carioca}.
 * </p>
 */
public class CariocaReducer extends DataReducer {

  private static List<CalculationParameter> calculationParameters = null;

  private static final double S = 35;

  private BigDecimalWithUncertainty A = null;

  private BigDecimalWithUncertainty B = null;

  private BigDecimalWithUncertainty C = null;

  private DoubleWithUncertainty RL = null;

  private DoubleWithUncertainty RH = null;

  private DoubleWithUncertainty R1 = null;

  private DoubleWithUncertainty a = null;

  private DoubleWithUncertainty b = null;

  private DoubleWithUncertainty c = null;

  private DoubleWithUncertainty k = null;

  private DoubleWithUncertainty kPrime = null;

  private DoubleWithUncertainty A_T = null;

  private DoubleWithUncertainty e1 = null;

  public CariocaReducer(Variable variable, Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) throws SensorTypeNotFoundException {
    super(variable, properties, calculationCoefficients);
  }

  @Override
  public void preprocess(Connection conn, Instrument instrument,
    DataSet dataset, List<Measurement> allMeasurements)
    throws DataReductionException {

    try {
      TimeDataSet castDataset = (TimeDataSet) dataset;

      A = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempA", castDataset.getStartTime()).getBigDecimalValue();

      B = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempB", castDataset.getStartTime()).getBigDecimalValue();

      C = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempC", castDataset.getStartTime()).getBigDecimalValue();

      RL = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempRL", castDataset.getStartTime()).getValue();

      RH = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempRH", castDataset.getStartTime()).getValue();

      R1 = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "tempR1", castDataset.getStartTime()).getValue();

      a = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "co2a", castDataset.getStartTime()).getValue();

      b = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "co2b", castDataset.getStartTime()).getValue();

      c = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "co2c", castDataset.getStartTime()).getValue();

      k = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "co2k", castDataset.getStartTime()).getValue();

      kPrime = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "co2k'", castDataset.getStartTime()).getValue();

      A_T = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "A_T", castDataset.getStartTime()).getValue();

      e1 = CalculationCoefficient.getCoefficient(calculationCoefficients,
        variable, "e1", castDataset.getStartTime()).getValue();
    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {

    try {
      // Get data values
      // Note that the nm values have reversed names because variables can't
      // begin
      // with numbers
      DoubleWithUncertainty Th = getAdjustedValue(measurement, "Th");
      DoubleWithUncertainty Refb = getAdjustedValue(measurement, "Refb");
      DoubleWithUncertainty Refh = getAdjustedValue(measurement, "Refh");
      DoubleWithUncertainty nm810 = measurement.getMeasurementValue("810nm")
        .getCalculatedValue();
      DoubleWithUncertainty nm596 = measurement.getMeasurementValue("596nm")
        .getCalculatedValue();
      DoubleWithUncertainty nm434 = measurement.getMeasurementValue("434nm")
        .getCalculatedValue();

      // Temperature calculation
      DoubleWithUncertainty K = calcK(Th, Refh, Refb);

      DoubleWithUncertainty R_temp = calcRtemp(K);

      // T = (1 / ( A + Bln(R) + C(ln(R))^3) ) - 273.15
      BigDecimalWithUncertainty lnR = new BigDecimalWithUncertainty(
        R_temp.log());
      BigDecimalWithUncertainty Bpart = B.multiply(lnR);
      BigDecimalWithUncertainty Cpart = C.multiply(lnR.pow(3));

      DoubleWithUncertainty kelvin = BigDecimalWithUncertainty.ONE
        .divide(A.add(Bpart).add(Cpart)).toDoubleWithUncertainty();

      DoubleWithUncertainty celsius = Calculators.celsius(kelvin);

      // CO2 calculation

      // e2 = 8.76277 - 0.04344*kelvin + 0.00007256*kelvin²
      DoubleWithUncertainty e2a = kelvin.multiply(0.04344);
      DoubleWithUncertainty e2b = kelvin.pow(2).multiply(0.00007256);
      DoubleWithUncertainty e2 = new DoubleWithUncertainty(8.76277)
        .subtract(e2a).add(e2b);

      // e3 = -0.005765 + 0.00058 * kelvin;
      DoubleWithUncertainty e3a = kelvin.multiply(0.00058);
      DoubleWithUncertainty e3 = e3a.subtract(0.005765);

      // Solubility coefficient (Weiss 1974)

      // aSPa = 0.023517 - 0.023656 * (kelvin / 100)
      // aSPb = 0.0047036 * (kelvin / 100)²
      // alphaS = S * (aSPa + aSPb)
      DoubleWithUncertainty hundredOverKelvin = DoubleWithUncertainty.HUNDRED
        .divide(kelvin);
      DoubleWithUncertainty kelvinOverHundred = kelvin
        .divide(DoubleWithUncertainty.HUNDRED);

      DoubleWithUncertainty aSPaRight = kelvinOverHundred.multiply(0.0236560);
      DoubleWithUncertainty aSPa = new DoubleWithUncertainty(0.023517)
        .subtract(aSPaRight);

      DoubleWithUncertainty aSPb = kelvinOverHundred.pow(2).multiply(0.0047036);

      // Note order of operation is left to right!
      DoubleWithUncertainty alphaS = aSPa.add(aSPb).multiply(S);

      // alphaA = -60.2409 + 93.4517 * 100/kelvin
      // alphaB = 23.3585 * ln(k/100)
      // alpha = exp(alphaA + alphaB + alphaS)

      DoubleWithUncertainty alphaA = hundredOverKelvin.multiply(93.4517)
        .subtract(60.2409);
      DoubleWithUncertainty alphaB = kelvinOverHundred.log().multiply(23.3585);
      DoubleWithUncertainty alpha = alphaA.add(alphaB).add(alphaS).exp();

      // Dissociation constants of carbonic acid in seawater (Lueker et al.,
      // 2000)

      Double pK_1 = 3633.86 / kelvin.value() - 61.2172
        + 9.6777 * Math.log(kelvin.value()) - 0.011555 * S
        + 0.0001152 * Math.pow(S, 2);

      Double K_1 = Math.pow(10, pK_1 * -1);

      Double pK_2 = 471.78 / kelvin.value() + 25.929
        - 3.16967 * Math.log(kelvin.value()) - 0.01781 * S
        + 0.0001122 * Math.pow(S, 2);

      Double K_2 = Math.pow(10, pK_2 * -1);

      // Thymol blue dissociation constant (Zhang and Byrne, 1996)
      Double pK_i = 4.706 * (S / kelvin.value()) + 26.33
        - 7.17218 * Math.log10(kelvin.value()) - 0.017316 * S;

      Double K_i = Math.pow(10, pK_i * -1);

      // Other parameters
      DoubleWithUncertainty A_434 = nm810.divide(nm434).log10().add(kPrime);
      DoubleWithUncertainty A_596 = nm810.divide(nm596).log10().add(k);

      DoubleWithUncertainty R = A_434.divide(A_596);

      // Amax = (e2 / (e2 - e1 * e3))
      // * ((e2 - e1) * A_434 + (1 - e3) * A_596);

      // Amax_a = e1 * e3
      // Amax_b = e2 - Amax_a
      // Amax_left = e2 / Amax_b

      // Amax_d = e2 - e1
      // Amax_e = Amax_d * A_434
      // Amax_f = 1 - e3
      // Amax_g = Amax_f * A_596
      // Amax_right = Amax_e + Amax_g

      // Amax = Amax_left * Amax_right

      DoubleWithUncertainty Amax_a = e1.multiply(e3);
      DoubleWithUncertainty Amax_b = e2.subtract(Amax_a);
      DoubleWithUncertainty Amax_left = e2.divide(Amax_b);

      DoubleWithUncertainty Amax_d = e2.subtract(e1);
      DoubleWithUncertainty Amax_e = Amax_d.multiply(A_434);
      DoubleWithUncertainty Amax_f = DoubleWithUncertainty.ONE.subtract(e3);
      DoubleWithUncertainty Amax_g = Amax_f.multiply(A_596);
      DoubleWithUncertainty Amax_right = Amax_e.add(Amax_g);

      DoubleWithUncertainty Amax = Amax_left.multiply(Amax_right);

      // X = (R * e2 - e3) / (1 - R * e1);

      DoubleWithUncertainty Xleft = R.multiply(e2).subtract(e3);
      DoubleWithUncertainty Xright = new DoubleWithUncertainty(1)
        .subtract(R.multiply(e1));
      DoubleWithUncertainty X = Xleft.divide(Xright);

      // pCO2 calculation

      // xTop_a = c / A_T
      // xTop_b = 1 + X
      // xTop_c = 1 / xTop_b
      // xTop = 1 - xTop_a * xTop_c

      DoubleWithUncertainty xTop_a = c.divide(A_T);
      DoubleWithUncertainty xTop_b = DoubleWithUncertainty.ONE.add(X);
      DoubleWithUncertainty xTop_c = DoubleWithUncertainty.ONE.divide(xTop_b);
      DoubleWithUncertainty xTop = DoubleWithUncertainty.ONE
        .subtract(xTop_a.multiply(xTop_c));

      // xBottom_a = (2 * K_2) / K_i
      // xBottom_b = 1 / X
      // xBottom_c = xBottom_a * xBottom_b
      // xBottom = 1 + xBottom_c

      DoubleWithUncertainty xBottom_a = new DoubleWithUncertainty(
        (2 * K_2) / K_i);
      DoubleWithUncertainty xBottom_b = DoubleWithUncertainty.ONE.divide(X);
      DoubleWithUncertainty xBottom_c = xBottom_a.multiply(xBottom_b);
      DoubleWithUncertainty xBottom = DoubleWithUncertainty.ONE.add(xBottom_c);

      // pCO2_a = K_i * A_T
      // pCO2_b = alpha * K_1
      // pCO2_c = pCO2_a / pCO2_b
      // pCO2_d = xTop / xBottom
      // pCO2 = p

      DoubleWithUncertainty pCO2_a = new DoubleWithUncertainty(K_i).divide(A_T);
      DoubleWithUncertainty pCO2_b = alpha.multiply(K_1);
      DoubleWithUncertainty pCO2_c = pCO2_a.divide(pCO2_b);
      DoubleWithUncertainty pCO2_d = xTop.divide(xBottom);

      DoubleWithUncertainty pCO2 = pCO2_c.multiply(X.multiply(pCO2_d))
        .multiply(Math.pow(10, 6));

      // pCO2 is slightly off which makes fCO2 quite a long way off
      // Double fCO2 = a * pCO2 + b;

      record.put("Water Temperature", celsius);
      record.put("K", K);
      record.put("R_temp", R_temp);
      record.put("lnR", lnR.toDoubleWithUncertainty());
      record.put("Bpart", Bpart.toDoubleWithUncertainty());
      record.put("Cpart", Cpart.toDoubleWithUncertainty());
      record.put("kelvin", kelvin);
      record.put("e2", e2);
      record.put("e3", e3);
      record.put("alphaSalinityPart", alphaS);
      record.put("alpha", alpha);
      record.put("pK_1", pK_1);
      record.put("K_1", K_1);
      record.put("pK_2", pK_2);
      record.put("K_2", K_2);
      record.put("pK_i", pK_i);
      record.put("K_i", K_i);
      record.put("A_434", A_434);
      record.put("A_596", A_596);
      record.put("R", R);
      record.put("X", X);
      record.put("xTop", xTop);
      record.put("xBottom", xBottom);
      record.put("Amax", Amax);
      record.put("pCO₂ SST", pCO2);
      // record.put("fCO₂", fCO2);

    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  private DoubleWithUncertainty calcK(DoubleWithUncertainty Th,
    DoubleWithUncertainty Refh, DoubleWithUncertainty Refb) {

    // (Th - Refh) / (Refb - Refh)
    DoubleWithUncertainty top = Th.subtract(Refh);
    DoubleWithUncertainty bottom = Refb.subtract(Refh);
    return top.divide(bottom);
  }

  private DoubleWithUncertainty calcRtemp(DoubleWithUncertainty K) {
    /*
     * ( // top R1 * (RH / (RH + R1)) + // t1 R1 * (K * RL) / (RL + R1) - // t2
     * R1 * (K * RH) / (RH + R1) // t3 ) / ( // bottom 1 - RH / (RH + R1) - //
     * b1 (K * RL) / (RL + R1) + // b2 (K * RH) / (RH + R1) // b3 )
     */

    // t1
    DoubleWithUncertainty t1Bottom = RH.add(R1);
    DoubleWithUncertainty t1Divide = RH.divide(t1Bottom);
    DoubleWithUncertainty t1 = R1.multiply(t1Divide);

    // t2
    DoubleWithUncertainty t2Top = K.multiply(RL);
    DoubleWithUncertainty t2Bottom = RL.add(R1);
    DoubleWithUncertainty t2Divide = t2Top.divide(t2Bottom);
    DoubleWithUncertainty t2 = R1.multiply(t2Divide);

    // t3
    DoubleWithUncertainty t3Top = K.multiply(RH);
    DoubleWithUncertainty t3Bottom = RH.add(R1);
    DoubleWithUncertainty t3Divide = t3Top.divide(t3Bottom);
    DoubleWithUncertainty t3 = R1.multiply(t3Divide);

    // top
    DoubleWithUncertainty top = t1.add(t2).subtract(t3);

    // b1
    DoubleWithUncertainty b1Bottom = RH.add(R1);
    DoubleWithUncertainty b1 = RH.divide(b1Bottom);

    // b2
    DoubleWithUncertainty b2Top = K.multiply(RL);
    DoubleWithUncertainty b2Bottom = RL.add(R1);
    DoubleWithUncertainty b2 = b2Top.divide(b2Bottom);

    // b3
    DoubleWithUncertainty b3Top = K.multiply(RH);
    DoubleWithUncertainty b3Bottom = RH.add(R1);
    DoubleWithUncertainty b3 = b3Top.divide(b3Bottom);

    // bottom
    DoubleWithUncertainty bottom = new DoubleWithUncertainty(1D).subtract(b1)
      .subtract(b2).add(b3);

    return top.divide(bottom);
  }

  private DoubleWithUncertainty getAdjustedValue(Measurement measurement,
    String sensor) throws SensorTypeNotFoundException {
    DoubleWithUncertainty measuredValue = measurement
      .getMeasurementValue(sensor).getCalculatedValue();

    if (measuredValue.value() > 4095 && measuredValue.value() < 8191) {
      measuredValue = measuredValue
        .subtract(new DoubleWithUncertainty(8192, 0));
    }

    return measuredValue;
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>();

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "Water Temperature", "Water Temperature", "TEMPPR01", "°C", false));

      calculationParameters.add(
        new CalculationParameter(makeParameterId(1), "K", "K", "K", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "R_temp", "R_temp", "R_temp", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "lnR", "lnR", "lnR", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "Bpart", "Bpart", "Bpart", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "Cpart", "Cpart", "Cpart", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(6),
        "kelvin", "kelvin", "kelvin", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(7),
        "e2", "e2", "e2", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(8),
        "e3", "e3", "e3", "", false));

      calculationParameters
        .add(new CalculationParameter(makeParameterId(9), "alphaSalinityPart",
          "alphaSalinityPart", "alphaSalinityPart", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(10),
        "alpha", "alpha", "alpha", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(11),
        "pK_1", "pK_1", "pK_1", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(12),
        "K_1", "K_1", "K_1", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(13),
        "pK_2", "pK_2", "pK_2", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(14),
        "K_2", "K_2", "K_2", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(15),
        "pK_i", "pK_i", "pK_i", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(16),
        "K_i", "K_i", "K_i", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(17),
        "A_434", "A_434", "A_434", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(18),
        "A_596", "A_596", "A_596", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(19),
        "R", "R", "R", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(20),
        "X", "X", "X", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(21),
        "xTop", "xTop", "xTop", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(22),
        "xBottom", "xBottom", "xBottom", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(23),
        "Amax", "Amax", "CARAMAX", "", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(24),
        "pCO₂ SST", "pCO₂ In Water", "PCO2TK02", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(25),
        "fCO₂", "fCO₂ In Water", "FCO2XXXX", "μatm", true));
    }

    return calculationParameters;
  }
}
