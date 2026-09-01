package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

public class UnderwayAtmospheric12_13Pco2ReducerTest2 extends DataReducerTest {

  @BeforeEach
  public void setup() {
    initResourceManager();
  }

  @AfterEach
  public void tearDown() {
    ResourceManager.destroy();
  }

  @FlywayTest
  @Test
  public void testSplitReduction() throws Exception {
    List<MeasurementValue> co2MeasurementValues = new ArrayList<MeasurementValue>();
    co2MeasurementValues.add(makeMeasurementValue("x¹²CO₂ (with standards)",
      new DoubleWithUncertainty(395.96D)));

    co2MeasurementValues.add(makeMeasurementValue("x¹³CO₂ (with standards)",
      new DoubleWithUncertainty(3.314D)));

    runTest(UnderwayMarine12_13Pco2Reducer.SPLIT_CO2_GAS_CAL_TYPE,
      co2MeasurementValues);
  }

  @FlywayTest
  @Test
  public void testTotalReduction() throws Exception {
    List<MeasurementValue> co2MeasurementValues = new ArrayList<MeasurementValue>();
    co2MeasurementValues.add(makeMeasurementValue(
      "x¹²CO₂ + x¹³CO₂ (with standards)", new DoubleWithUncertainty(399.274D)));

    runTest(UnderwayMarine12_13Pco2Reducer.TOTAL_CO2_GAS_CAL_TYPE,
      co2MeasurementValues);
  }

  private void runTest(String calType,
    List<MeasurementValue> co2MeasurementValues) throws Exception {

    Properties varProps = new Properties();
    varProps.put(UnderwayMarine12_13Pco2Reducer.CAL_GAS_TYPE_ATTR, calType);
    HashMap<String, Properties> props = new HashMap<String, Properties>();
    props.put("Underway Atmospheric pCO₂ from ¹²CO₂/¹³CO₂", varProps);

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = ResourceManager.getInstance().getSensorsConfiguration()
      .getInstrumentVariable("Underway Atmospheric pCO₂ from ¹²CO₂/¹³CO₂");

    // Initialise the reducer
    UnderwayAtmospheric12_13Pco2Reducer reducer = new UnderwayAtmospheric12_13Pco2Reducer(
      variable, props, null);

    List<MeasurementValue> allMeasurementValues = new ArrayList<MeasurementValue>();
    allMeasurementValues.add(makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(6.061D)));
    allMeasurementValues.add(
      makeMeasurementValue("Salinity", new DoubleWithUncertainty(34.441D)));
    allMeasurementValues.add(makeMeasurementValue("Atmospheric Pressure",
      new DoubleWithUncertainty(1023.58D)));

    allMeasurementValues.addAll(co2MeasurementValues);

    Measurement measurement = makeMeasurement(
      allMeasurementValues.toArray(MeasurementValue[]::new));

    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    assertEquals(0.00909D, record.getCalculationValue("pH₂O").value(), 0.0001);
    assertEquals(399.71652D, record.getCalculationValue("pCO₂").value(),
      0.0001);
    assertEquals(398.0793D, record.getCalculationValue("fCO₂").value(), 0.0001);
    assertTrue(false, "Uncertainty");

  }
}
