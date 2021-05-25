package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;

/**
 * Data Reduction class for underway marine pCO₂
 *
 * @author Steve Jones
 *
 */
public class UnderwayAtmospheric12_13Pco2Reducer extends DataReducer {

  private static List<CalculationParameter> calculationParameters = null;

  public UnderwayAtmospheric12_13Pco2Reducer(Variable variable,
    Map<String, Properties> properties) {

    super(variable, properties);
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws Exception {

    // We use equilibrator temperature as the presumed most realistic gas
    // temperature
    Double equilibratorTemperature = measurement
      .getMeasurementValue("Equilibrator Temperature").getCalculatedValue();
    Double salinity = measurement.getMeasurementValue("Salinity")
      .getCalculatedValue();
    Double atmosphericPressure = measurement
      .getMeasurementValue("Atmospheric Pressure").getCalculatedValue();
    Double x12CO2InGas = measurement
      .getMeasurementValue("x¹²CO₂ (with standards)").getCalculatedValue();
    Double x13CO2InGas = measurement
      .getMeasurementValue("x¹³CO₂ (with standards)").getCalculatedValue();

    Double seaLevelPressure = Calculators.calcSeaLevelPressure(
      atmosphericPressure, equilibratorTemperature,
      getFloatProperty("atm_pres_sensor_height"));

    Double pH2O = Calculators.calcPH2O(salinity, equilibratorTemperature);

    Double p12CO2 = Calculators.calcpCO2TEWet(x12CO2InGas, seaLevelPressure,
      pH2O);
    Double f12CO2 = Calculators.calcfCO2(p12CO2, x12CO2InGas, seaLevelPressure,
      equilibratorTemperature);

    Double p13CO2 = Calculators.calcpCO2TEWet(x13CO2InGas, seaLevelPressure,
      pH2O);
    Double f13CO2 = Calculators.calcfCO2(p13CO2, x13CO2InGas, seaLevelPressure,
      equilibratorTemperature);

    Double pCO2 = p12CO2 + p13CO2;
    Double fCO2 = f12CO2 + f13CO2;

    record.put("Sea Level Pressure", seaLevelPressure);
    record.put("pH₂O", pH2O);
    record.put("p¹²CO₂", p12CO2);
    record.put("f¹²CO₂", f12CO2);
    record.put("p¹³CO₂", p13CO2);
    record.put("f¹³CO₂", f13CO2);
    record.put("pCO₂", pCO2);
    record.put("fCO₂", fCO2);
  }

  @Override
  protected String[] getRequiredTypeStrings() {
    return new String[] { "Equilibrator Temperature", "Salinity",
      "Atmospheric Pressure", "x¹²CO₂ (with standards)",
      "x¹³CO₂ (with standards)" };
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(8);

      calculationParameters.add(new CalculationParameter(makeParameterId(0),
        "Sea Level Pressure", "Sea Level Pressure", "CAPASS01", "hPa", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "pH₂O", "Atmosphere Water Vapour Pressure", "CPVPZZ01", "hPa", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "p¹²CO₂", "p¹²CO₂ In Atmosphere", "A12CO2XXXX", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "f¹²CO₂", "f¹²CO₂ In Atmosphere", "F12CO2WTAT", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "p¹³CO₂", "p¹³CO₂ In Atmosphere", "A13CO2XXXX", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "f¹³CO₂", "f¹³CO₂ In Atmosphere", "F13CO2WTAT", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(6),
        "pCO₂", "pCO₂ In Atmosphere", "ACO2XXXX", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(7),
        "fCO₂", "fCO₂ In Atmosphere", "FCO2WTAT", "μatm", true));
    }

    return calculationParameters;
  }
}
