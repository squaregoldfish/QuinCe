package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

public class UnderwayAtmospheric12_13Pco2ReducerTest extends DataReducerTest {

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
      new DoubleWithUncertainty(395.96D, 0.4F)));

    co2MeasurementValues.add(makeMeasurementValue("x¹³CO₂ (with standards)",
      new DoubleWithUncertainty(3.314D, 0.4F)));

    HashMap<String, DoubleWithUncertainty> expectedValues = new HashMap<String, DoubleWithUncertainty>();
    expectedValues.put("pH₂O", new DoubleWithUncertainty(0.00909D, 0.00009F));
    expectedValues.put("pCO₂", new DoubleWithUncertainty(399.71652D, 0.5685F));
    expectedValues.put("fCO₂", new DoubleWithUncertainty(398.0793D, 0.5676F));

    runTest(UnderwayMarine12_13Pco2Reducer.SPLIT_CO2_GAS_CAL_TYPE,
      co2MeasurementValues, expectedValues);
  }

  @FlywayTest
  @Test
  public void testTotalReduction() throws Exception {
    List<MeasurementValue> co2MeasurementValues = new ArrayList<MeasurementValue>();
    co2MeasurementValues
      .add(makeMeasurementValue("x¹²CO₂ + x¹³CO₂ (with standards)",
        new DoubleWithUncertainty(399.274D, 0.4F)));

    HashMap<String, DoubleWithUncertainty> expectedValues = new HashMap<String, DoubleWithUncertainty>();
    expectedValues.put("pH₂O", new DoubleWithUncertainty(0.00909D, 0.00009F));
    expectedValues.put("pCO₂", new DoubleWithUncertainty(399.71652D, 0.4036F));
    expectedValues.put("fCO₂", new DoubleWithUncertainty(398.0793D, 0.404F));

    runTest(UnderwayMarine12_13Pco2Reducer.TOTAL_CO2_GAS_CAL_TYPE,
      co2MeasurementValues, expectedValues);
  }

  private void runTest(String calType,
    List<MeasurementValue> co2MeasurementValues,
    Map<String, DoubleWithUncertainty> expectedValues) throws Exception {

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
      new DoubleWithUncertainty(6.061D, 0.1F)));
    allMeasurementValues.add(makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(34.441D, 0.05F)));
    allMeasurementValues.add(makeMeasurementValue("Atmospheric Pressure",
      new DoubleWithUncertainty(1023.58D, 0.1F)));

    allMeasurementValues.addAll(co2MeasurementValues);

    Measurement measurement = makeMeasurement(
      allMeasurementValues.toArray(MeasurementValue[]::new));

    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pH₂O"), "pH₂O")
      .matches(expectedValues.get("pH₂O"));

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂"), "pCO₂")
      .matches(expectedValues.get("pCO₂"));

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(expectedValues.get("fCO₂"));
  }
}
