package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.Calibration.CalibrationSet;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorType;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

/**
 * Data Reduction class for underway marine pCO₂ measurements taken by
 * equilibrator-based systems.
 */
public class UnderwayMarinePco2Reducer extends DataReducer {

  /**
   * The reducer's calculation parameters.
   */
  private static List<CalculationParameter> calculationParameters = null;

  /**
   * Basic {@link DataReducer} constructor.
   *
   * @param variable
   *          The {@link Variable} being processed.
   * @param properties
   *          The variable properties.
   * @param calculationCoefficients
   *          The calculation coefficients specified for the instrument.
   */
  public UnderwayMarinePco2Reducer(Variable variable,
    Map<String, Properties> properties,
    CalibrationSet calculationCoefficients) {
    super(variable, properties, calculationCoefficients);
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws DataReductionException {

    try {
      DoubleWithUncertainty waterTemperature = measurement
        .getMeasurementValue("Water Temperature").getCalculatedValue();
      DoubleWithUncertainty salinity = measurement
        .getMeasurementValue("Salinity").getCalculatedValue();
      DoubleWithUncertainty equilibratorTemperature = measurement
        .getMeasurementValue("Equilibrator Temperature").getCalculatedValue();
      DoubleWithUncertainty equilibratorPressure = measurement
        .getMeasurementValue("Equilibrator Pressure").getCalculatedValue();
      DoubleWithUncertainty xCO2 = measurement
        .getMeasurementValue(getXCO2Parameter()).getCalculatedValue();

      Calculator calculator = new Calculator(waterTemperature, salinity,
        equilibratorTemperature, equilibratorPressure, xCO2);

      // Store the calculated values
      DoubleWithUncertainty deltaT = equilibratorTemperature
        .subtract(waterTemperature);
      record.put("ΔT", deltaT);

      // If the ΔT is really large, we don't store any other values.
      if (Math.abs(deltaT.value()) < 100D) {
        record.put("pH₂O", calculator.pH2O);
        record.put("pCO₂ TE Wet", calculator.pCo2TEWet);
        record.put("fCO₂ TE Wet", calculator.fCo2TEWet);
        record.put("pCO₂ SST", calculator.pCO2SST);
        record.put("fCO₂", calculator.fCO2);
      } else {
        record.put("pH₂O", Double.NaN);
        record.put("pCO₂ TE Wet", Double.NaN);
        record.put("fCO₂ TE Wet", Double.NaN);
        record.put("pCO₂ SST", Double.NaN);
        record.put("fCO₂", Double.NaN);
      }

    } catch (Exception e) {
      throw new DataReductionException(e);
    }
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(6);

      calculationParameters
        .add(new CalculationParameter(makeParameterId(0), "ΔT",
          "Water-Equilibrator Temperature Difference", "DELTAT", "°C", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "pH₂O", "Marine Water Vapour Pressure", "RH2OX0EQ", "hPa", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "pCO₂ TE Wet", "pCO₂ In Water - Equilibrator Temperature", "PCO2IG02",
        "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "fCO₂ TE Wet", "fCO₂ In Water - Equilibrator Temperature", "FCO2IG02",
        "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "pCO₂ SST", "pCO₂ In Water", "PCO2TK02", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "fCO₂", "fCO₂ In Water", "FCO2XXXX", "μatm", true));
    }

    return calculationParameters;
  }

  /**
   * Get the name of the {@link SensorType} to use to get the base xCO₂ value
   * for the calculation.
   *
   * @return The xCO₂ {@link SensorType} to use.
   */
  protected String getXCO2Parameter() {
    return "xCO₂ (with standards)";
  }

  /**
   * Class that takes in the required parameters and calculates pCO₂ and fCO₂.
   */
  class Calculator {

    /**
     * Measured water temperature.
     */
    private final DoubleWithUncertainty waterTemperature;

    /**
     * Measured salinity.
     */
    private final DoubleWithUncertainty salinity;

    /**
     * Measured temperature inside the equilibrator.
     */
    private final DoubleWithUncertainty equilibratorTemperature;

    /**
     * Measured pressure inside the equilibrator.
     */
    private final DoubleWithUncertainty equilibratorPressure;

    /**
     * The CO₂ value measured by the gas analyser.
     */
    private final DoubleWithUncertainty co2InGas;

    /**
     * Calculated water vapour pressure.
     */
    protected DoubleWithUncertainty pH2O = null;

    /**
     * Calculated pCO₂ at 100% humidity at equilibrator temperature.
     */
    protected DoubleWithUncertainty pCo2TEWet = null;

    /**
     * Calculated fCO₂ at 100% humidity at equilibrator temperature.
     */
    protected DoubleWithUncertainty fCo2TEWet = null;

    /**
     * Calculated pCO₂ at 100% humidity at water temperature.
     */
    protected DoubleWithUncertainty pCO2SST = null;

    /**
     * Calculated fCO₂ at 100% humidity at water temperature.
     */
    protected DoubleWithUncertainty fCO2 = null;

    /**
     * Initialise the calculator with the required measured values.
     *
     * @param waterTemperature
     *          Water temperature.
     * @param salinity
     *          Salinity.
     * @param equilibratorTemperature
     *          Equlibrator temperature.
     * @param equilibratorPressure
     *          Equilibrator pressure.
     * @param co2InGas
     *          Measure CO₂.
     */
    protected Calculator(DoubleWithUncertainty waterTemperature,
      DoubleWithUncertainty salinity,
      DoubleWithUncertainty equilibratorTemperature,
      DoubleWithUncertainty equilibratorPressure,
      DoubleWithUncertainty co2InGas) {

      this.waterTemperature = waterTemperature;
      this.salinity = salinity;
      this.equilibratorTemperature = equilibratorTemperature;
      this.equilibratorPressure = equilibratorPressure;
      this.co2InGas = co2InGas;
      calculate();
    }

    /**
     * Perform the calculation to produce {@link #pH2O}, and the pCO₂/fCO₂
     * combinations.
     */
    protected void calculate() {
      pH2O = Calculators.calcPH2O(salinity, equilibratorTemperature);
      pCo2TEWet = Calculators.calcpCO2TEWet(co2InGas, equilibratorPressure,
        pH2O);
      fCo2TEWet = Calculators.calcfCO2(pCo2TEWet, co2InGas,
        equilibratorPressure, equilibratorTemperature);
      pCO2SST = Calculators.calcCO2AtSST(pCo2TEWet, equilibratorTemperature,
        waterTemperature);
      fCO2 = Calculators.calcCO2AtSST(fCo2TEWet, equilibratorTemperature,
        waterTemperature);
    }
  }
}
