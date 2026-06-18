package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableDouble;

import uk.ac.exeter.QuinCe.data.Dataset.DataSet;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetMeasurements;
import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.HaganGenXMeasurementLocator;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementLocatorException;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValueCollector;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValueCollectorFactory;
import uk.ac.exeter.QuinCe.data.Dataset.TimeCoordinate;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

/**
 * Reducer for Hagan GenX water measurements.
 *
 * <p>
 * Most of the calculation code is translated from the official processing
 * software.
 * </p>
 */
public class HaganGenXEqReducer extends DataReducer {

  private static final double p0 = 99.0;

  // # Pressure correction Coeficients
  private static final double b1 = 1.101583;
  private static final double b2 = -0.006121779;
  private static final double b3 = -0.2662779;
  private static final double b4 = 3.698951;
  private static final double b5 = 0.496099382;

  // #Mole Fraction coeficients
  private static final double a1 = 0.39899740;
  private static final double a2 = 18.24935912; // these have been divided by
                                                // (To(50) +
  // 273.15) from original and offset the term
  // in the final temp correction
  private static final double a3 = 0.097101982;
  private static final double a4 = 1.845891413; // these have been divided by
                                                // (To(50) +
  // 273.15) from original and offset the term
  // in the final temp correction

  private static final double A = 16.403467707; // A = a2 - a4
  private static final double B = -33.972994517484474738; // B = 2 * A* ((a1*
                                                          // a4) - (a2* a3))
  private static final double D = 2.508554815; // D = (a3* a2) + (a1* a4)

  private static List<CalculationParameter> calculationParameters = null;

  private TreeMap<TimeCoordinate, DataReductionElement> zeroCalKs = new TreeMap<TimeCoordinate, DataReductionElement>();

  private TreeMap<TimeCoordinate, DataReductionElement> spanCalKs = new TreeMap<TimeCoordinate, DataReductionElement>();

  public HaganGenXEqReducer(Variable variable,
    Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

  public void preprocess(Connection conn, Instrument instrument,
    DataSet dataset, DatasetSensorValues allSensorValues,
    DatasetMeasurements allMeasurements) throws DataReductionException {

    try {
      SensorType tempSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX Temperature");
      SensorType pressureSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX Pressure");
      SensorType co2Raw1SensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX CO₂ Raw 1");
      SensorType co2Raw2SensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX CO₂ Raw 2");
      SensorType rhSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX Relative Humidity");
      SensorType rhTempSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration()
        .getSensorType("GenX Relative Humidity Temperature");
      SensorType calKSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX CALK");
      SensorType spanRefSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX Span Ref");
      SensorType spanSlopeSensorType = ResourceManager.getInstance()
        .getSensorsConfiguration().getSensorType("GenX Span Slope");

      MeasurementValueCollector measurementValueCollector = MeasurementValueCollectorFactory
        .getCollector(variable);

      // Process Zero measurements
      for (Measurement measurement : allMeasurements.getOrderedMeasurements()) {
        if (HaganGenXMeasurementLocator.getRunType(measurement)
          .equals("zero")) {

          Collection<MeasurementValue> measurementValues = measurementValueCollector
            .collectMeasurementValues(instrument, dataset, variable,
              allMeasurements, allSensorValues, conn, measurement);

          MeasurementValue co2Raw1 = getMeasurementValue(measurementValues,
            co2Raw1SensorType);
          MeasurementValue co2Raw2 = getMeasurementValue(measurementValues,
            co2Raw2SensorType);

          DataReductionElement zeroCalK = new DataReductionElement();
          zeroCalK.setValue(
            co2Raw2.getCalculatedValue() / co2Raw1.getCalculatedValue());
          zeroCalK.addSensorValueIDs(co2Raw1);
          zeroCalK.addSensorValueIDs(co2Raw2);

          zeroCalKs.put((TimeCoordinate) measurement.getCoordinate(), zeroCalK);
        }
      }

      // Process Span measurements
      for (Measurement measurement : allMeasurements.getOrderedMeasurements()) {
        if (HaganGenXMeasurementLocator.getRunType(measurement)
          .equals("span")) {

          Collection<MeasurementValue> measurementValues = measurementValueCollector
            .collectMeasurementValues(instrument, dataset, variable,
              allMeasurements, allSensorValues, conn, measurement);

          MeasurementValue temp = getMeasurementValue(measurementValues,
            tempSensorType);
          MeasurementValue pressure = getMeasurementValue(measurementValues,
            pressureSensorType);
          MeasurementValue co2Raw1 = getMeasurementValue(measurementValues,
            co2Raw1SensorType);
          MeasurementValue co2Raw2 = getMeasurementValue(measurementValues,
            co2Raw2SensorType);
          MeasurementValue rh = getMeasurementValue(measurementValues,
            rhSensorType);
          MeasurementValue rhTemp = getMeasurementValue(measurementValues,
            rhTempSensorType);

          DataReductionElement zeroCalK = DataReductionElement
            .getInterpolatedElement(
              (TimeCoordinate) measurement.getCoordinate(), zeroCalKs);

          MeasurementValue measurementCalK = getMeasurementValue(
            measurementValues, calKSensorType);

          MeasurementValue spanRef = getMeasurementValue(measurementValues,
            spanRefSensorType);

          MeasurementValue spanSlope = getMeasurementValue(measurementValues,
            spanSlopeSensorType);

          double spanCalK = convergeSpanCalK(temp.getCalculatedValue(),
            pressure.getCalculatedValue(), co2Raw1.getCalculatedValue(),
            co2Raw2.getCalculatedValue(), rh.getCalculatedValue(),
            rhTemp.getCalculatedValue(), zeroCalK.getValue(),
            measurementCalK.getCalculatedValue(), spanRef.getCalculatedValue(),
            spanSlope.getCalculatedValue());

          DataReductionElement spanCalKElement = new DataReductionElement();
          spanCalKElement.setValue(spanCalK);
          spanCalKElement.addSensorValueIDs(temp, pressure, co2Raw1, co2Raw2,
            rh, rhTemp, measurementCalK, spanRef, spanSlope);
          spanCalKElement.addSensorValueIDs(zeroCalK);
        }
      }

    } catch (MeasurementLocatorException e) {
      throw new DataReductionException(e.getMessage());
    } catch (Exception e) {
      throw new DataReductionException(e);
    }
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

  private MeasurementValue getMeasurementValue(
    Collection<MeasurementValue> measurementValues, SensorType sensorType)
    throws DataReductionException {
    return measurementValues.stream()
      .filter(mv -> mv.getSensorType().equals(sensorType)).findAny()
      .orElseThrow(() -> new DataReductionException(
        "Missing " + sensorType.getShortName()));

  }

  private double convergeSpanCalK(double temp, double pressure, double raw1,
    double raw2, double rh, double rhTemp, double zeroCalK, double spanCalKseed,
    double spanRef, double spanSlope) {

    double stepDecimal = 0.01;
    double calcCO2, previousCalcCO2;
    double calcSpanCalK = spanCalKseed;

    MutableDouble r_absp = new MutableDouble(Double.NaN);
    MutableDouble s_absp = new MutableDouble(Double.NaN);

    if (temp == 0 || pressure == 0 || raw1 == 0 || raw2 == 0 || zeroCalK == 0
      || spanCalKseed == 0 || spanRef == 0)
      return (0);

    calcCO2 = calculatedCO2(temp, pressure, raw1, raw2, rh, rhTemp, zeroCalK,
      calcSpanCalK, r_absp, s_absp, spanSlope);
    while (stepDecimal > 0.00000001) {
      calcSpanCalK += (calcCO2 > spanRef) ? -stepDecimal : stepDecimal;
      previousCalcCO2 = calcCO2;
      calcCO2 = calculatedCO2(temp, pressure, raw1, raw2, rh, rhTemp, zeroCalK,
        calcSpanCalK, r_absp, s_absp, spanSlope);
      if ((calcCO2 > spanRef) ^ (previousCalcCO2 > spanRef)) {
        calcSpanCalK -= (previousCalcCO2 > spanRef) ? -stepDecimal
          : stepDecimal;
        calcCO2 = previousCalcCO2;
        stepDecimal /= 10;
      }
    }

    return (calcSpanCalK);
  }

  private double calculatedCO2(double temp, double pressure, double raw1,
    double raw2, double rh, double rhTemp, double zeroCalK, double spanCalK,
    MutableDouble r_absp, MutableDouble s_absp, double spanSlope) {

    double p_absp, P, a_1, b_1, x, g, c_1, c_2, c_3, C, wc;
    // A = a2 - a4
    // B = 2 * A* ((a1* a4) - (a2* a3))
    // D = (a3* a2) + (a1* a4)

    r_absp.setValue(1.0 - ((raw1 / raw2) * zeroCalK));

    // po is std pressure, po = 99
    // P is the ratio of the std pressure and measured press
    g = 1.0;
    if (pressure != p0) {
      if (pressure < p0)
        P = p0 / pressure;
      else if (pressure > p0)
        P = pressure / p0;
      else
        P = 1.0;

      // g is the empirical correction function and is a function of absorptance
      // and pressure
      a_1 = (1 / (b1 * (P - 1)));
      b_1 = (1 / (b5 - r_absp.getValue())) - (1 / b5);
      c_1 = (1 / (b2 + (b3 * P))) + b4;
      x = 1 + (1 / (a_1 + (b_1 / c_1)));
      if (pressure < p0)
        g = x;
      else if (pressure > p0)
        g = 1.0 / x;
    }

    // the order of the next 2 lines was under discussion but if span slope is
    // used, this is the correct order for both Li820 and Li830
    s_absp.setValue(
      r_absp.getValue() * (spanCalK + (r_absp.getValue() * spanSlope)));
    p_absp = s_absp.getValue() * g;

    // Computing CO2 Mole Fraction
    c_1 = D - ((a2 + a4) * p_absp);
    c_2 = ((A * A) * (p_absp * p_absp)) + (B * p_absp) + (D * D);
    c_3 = p_absp - a1 - a3;
    C = (c_1 - Math.sqrt(c_2)) / (2 * c_3) * (temp + 273.15);

    // moisture compensation - band broadening effect
    // h=1.45
    // wc = 1 + (h-1) * xH2O(umol/mol) * 1E-6
    // xH2O(umol/mol) =
    // 1260*216.7*(RH/100.0*6.112*exp(17.62*T/(243.12+T))/(273.15+T)) where
    // RH(%) & T(C)
    wc = 1 + 0.007509747 * rh * Math.exp(17.62 * rhTemp / (243.12 + rhTemp))
      / (273.15 + rhTemp);
    C *= wc;

    return (C);
  }

}
