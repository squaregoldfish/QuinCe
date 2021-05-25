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
public class UnderwayMarine12_13Pco2Reducer extends DataReducer {

  private static List<CalculationParameter> calculationParameters = null;

  public UnderwayMarine12_13Pco2Reducer(Variable variable,
    Map<String, Properties> properties) {
    super(variable, properties);
  }

  @Override
  public void doCalculation(Instrument instrument, Measurement measurement,
    DataReductionRecord record, Connection conn) throws Exception {

    Double intakeTemperature = measurement
      .getMeasurementValue("Intake Temperature").getCalculatedValue();
    Double salinity = measurement.getMeasurementValue("Salinity")
      .getCalculatedValue();
    Double equilibratorTemperature = measurement
      .getMeasurementValue("Equilibrator Temperature").getCalculatedValue();
    Double equilibratorPressure = measurement
      .getMeasurementValue("Equilibrator Pressure").getCalculatedValue();

    Double x12CO2InGas = measurement
      .getMeasurementValue("x¹²CO₂ (with standards)").getCalculatedValue();
    Double x13CO2InGas = measurement
      .getMeasurementValue("x¹³CO₂ (with standards)").getCalculatedValue();

    Double pH2O = Calculators.calcPH2O(salinity, equilibratorTemperature);

    Double p12Co2TEWet = Calculators.calcpCO2TEWet(x12CO2InGas,
      equilibratorPressure, pH2O);
    Double f12Co2TEWet = Calculators.calcfCO2(p12Co2TEWet, x12CO2InGas,
      equilibratorPressure, equilibratorTemperature);

    Double p13Co2TEWet = Calculators.calcpCO2TEWet(x13CO2InGas,
      equilibratorPressure, pH2O);
    Double f13Co2TEWet = Calculators.calcfCO2(p13Co2TEWet, x12CO2InGas,
      equilibratorPressure, equilibratorTemperature);

    Double pCO2SST = Calculators.calcCO2AtSST(p12Co2TEWet + p13Co2TEWet,
      equilibratorTemperature, intakeTemperature);

    Double fCO2 = Calculators.calcCO2AtSST(f12Co2TEWet + f13Co2TEWet,
      equilibratorTemperature, intakeTemperature);

    // Store the calculated values
    record.put("ΔT", Math.abs(intakeTemperature - equilibratorTemperature));
    record.put("pH₂O", pH2O);
    record.put("p¹²CO₂ TE Wet", p12Co2TEWet);
    record.put("f¹²CO₂ TE Wet", f12Co2TEWet);
    record.put("p¹³CO₂ TE Wet", p13Co2TEWet);
    record.put("f¹³CO₂ TE Wet", f13Co2TEWet);
    record.put("pCO₂ SST", pCO2SST);
    record.put("fCO₂", fCO2);
  }

  @Override
  protected String[] getRequiredTypeStrings() {
    return new String[] { "Intake Temperature", "Salinity",
      "Equilibrator Temperature", "Equilibrator Pressure",
      "x¹²CO₂ (with standards)", "x¹³CO₂ (with standards)" };
  }

  @Override
  public List<CalculationParameter> getCalculationParameters() {
    if (null == calculationParameters) {
      calculationParameters = new ArrayList<CalculationParameter>(8);

      calculationParameters
        .add(new CalculationParameter(makeParameterId(0), "ΔT",
          "Water-Equilibrator Temperature Difference", "DELTAT", "°C", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(1),
        "pH₂O", "Marine Water Vapour Pressure", "RH2OX0EQ", "hPa", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(2),
        "p¹²CO₂ TE Wet", "p¹²CO₂ In Water - Equilibrator Temperature",
        "P12CO2IG02", "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(3),
        "f¹²CO₂ TE Wet", "f¹²CO₂ In Water - Equilibrator Temperature",
        "F12CO2IG02", "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(4),
        "p¹³CO₂ TE Wet", "p¹³CO₂ In Water - Equilibrator Temperature",
        "P13CO2IG02", "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(5),
        "f¹³CO₂ TE Wet", "f¹³CO₂ In Water - Equilibrator Temperature",
        "F13CO2IG02", "μatm", false));

      calculationParameters.add(new CalculationParameter(makeParameterId(6),
        "pCO₂ SST", "pCO₂ In Water", "PCO2TK02", "μatm", true));

      calculationParameters.add(new CalculationParameter(makeParameterId(7),
        "fCO₂", "fCO₂ In Water", "FCO2XXXX", "μatm", true));
    }

    return calculationParameters;
  }
}
