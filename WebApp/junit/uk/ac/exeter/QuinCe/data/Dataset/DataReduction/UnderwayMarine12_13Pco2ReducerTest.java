package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;
import uk.ac.exeter.QuinCe.web.system.ResourceManager;

public class UnderwayMarine12_13Pco2ReducerTest extends DataReducerTest {

  @BeforeEach
  public void setup() {
    initResourceManager();
  }

  @AfterEach
  public void tearDown() {
    ResourceManager.destroy();
  }

  @FlywayTest
  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  public void testSplitReduction(boolean largeDeltaT) throws Exception {
    List<MeasurementValue> measurementValues = new ArrayList<MeasurementValue>();

    measurementValues.add(makeMeasurementValue("Equilibrator Temperature",
      new DoubleWithUncertainty(largeDeltaT ? 1000D : 7.513D, 0.1F)));

    measurementValues.add(makeMeasurementValue("x¹²CO₂ (with standards)",
      new DoubleWithUncertainty(395.96D, 0.4F)));

    measurementValues.add(makeMeasurementValue("x¹³CO₂ (with standards)",
      new DoubleWithUncertainty(3.314D, 0.4F)));

    HashMap<String, DoubleWithUncertainty> expectedValues = new HashMap<String, DoubleWithUncertainty>();

    if (largeDeltaT) {
      expectedValues.put("ΔT", new DoubleWithUncertainty(993.939D, 0.1118F));
      expectedValues.put("pH₂O", new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("pCO₂ TE Wet",
        new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("fCO₂ TE Wet",
        new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("pCO₂ SST", new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("fCO₂", new DoubleWithUncertainty(Double.NaN, 0F));
    } else {
      expectedValues.put("ΔT", new DoubleWithUncertainty(1.452D, 0.1118F));
      expectedValues.put("pH₂O",
        new DoubleWithUncertainty(0.010004D, 0.00009F));
      expectedValues.put("pCO₂ TE Wet",
        new DoubleWithUncertainty(398.0550D, 0.5664F));
      expectedValues.put("fCO₂ TE Wet",
        new DoubleWithUncertainty(396.4604D, 0.5656F));
      expectedValues.put("pCO₂ SST",
        new DoubleWithUncertainty(374.3423D, 1.8348F));
      expectedValues.put("fCO₂", new DoubleWithUncertainty(372.8427D, 1.8278F));
    }

    runTest(UnderwayMarine12_13Pco2Reducer.SPLIT_CO2_GAS_CAL_TYPE,
      measurementValues, expectedValues);
  }

  @FlywayTest
  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  public void testTotalReduction(boolean largeDeltaT) throws Exception {
    List<MeasurementValue> measurementValues = new ArrayList<MeasurementValue>();

    measurementValues.add(makeMeasurementValue("Equilibrator Temperature",
      new DoubleWithUncertainty(largeDeltaT ? 1000D : 7.513D, 0.1F)));

    measurementValues
      .add(makeMeasurementValue("x¹²CO₂ + x¹³CO₂ (with standards)",
        new DoubleWithUncertainty(399.274D, 0.4F)));

    HashMap<String, DoubleWithUncertainty> expectedValues = new HashMap<String, DoubleWithUncertainty>();

    if (largeDeltaT) {
      expectedValues.put("ΔT", new DoubleWithUncertainty(993.939D, 0.1118F));
      expectedValues.put("pH₂O", new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("pCO₂ TE Wet",
        new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("fCO₂ TE Wet",
        new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("pCO₂ SST", new DoubleWithUncertainty(Double.NaN, 0F));
      expectedValues.put("fCO₂", new DoubleWithUncertainty(Double.NaN, 0F));
    } else {
      expectedValues.put("ΔT", new DoubleWithUncertainty(1.452D, 0.1118F));
      expectedValues.put("pH₂O",
        new DoubleWithUncertainty(0.010004D, 0.00009F));
      expectedValues.put("pCO₂ TE Wet",
        new DoubleWithUncertainty(398.0550D, 0.4022F));
      expectedValues.put("fCO₂ TE Wet",
        new DoubleWithUncertainty(396.4604D, 0.4027F));
      expectedValues.put("pCO₂ SST",
        new DoubleWithUncertainty(374.3423D, 1.8103F));
      expectedValues.put("fCO₂", new DoubleWithUncertainty(372.8427D, 1.8034F));
    }

    runTest(UnderwayMarine12_13Pco2Reducer.TOTAL_CO2_GAS_CAL_TYPE,
      measurementValues, expectedValues);
  }

  private void runTest(String calType,
    List<MeasurementValue> co2MeasurementValues,
    Map<String, DoubleWithUncertainty> expectedValues) throws Exception {

    Properties varProps = new Properties();
    varProps.put(UnderwayMarine12_13Pco2Reducer.CAL_GAS_TYPE_ATTR, calType);
    HashMap<String, Properties> props = new HashMap<String, Properties>();
    props.put("Underway Marine pCO₂ from ¹²CO₂/¹³CO₂", varProps);

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = ResourceManager.getInstance().getSensorsConfiguration()
      .getInstrumentVariable("Underway Marine pCO₂ from ¹²CO₂/¹³CO₂");

    // Initialise the reducer
    UnderwayMarine12_13Pco2Reducer reducer = new UnderwayMarine12_13Pco2Reducer(
      variable, props, null);

    List<MeasurementValue> allMeasurementValues = new ArrayList<MeasurementValue>();
    allMeasurementValues.add(makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(6.061D, 0.05F)));
    allMeasurementValues.add(makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(34.441D, 0.03F)));
    allMeasurementValues.add(makeMeasurementValue("Equilibrator Pressure",
      new DoubleWithUncertainty(1020.33D, 0.1F)));

    allMeasurementValues.addAll(co2MeasurementValues);

    Measurement measurement = makeMeasurement(
      allMeasurementValues.toArray(MeasurementValue[]::new));

    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("ΔT"), "ΔT")
      .matches(expectedValues.get("ΔT"));
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pH₂O"), "pH₂O")
      .matches(expectedValues.get("pH₂O"));
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ TE Wet"), "pCO₂ TE Wet")
      .matches(expectedValues.get("pCO₂ TE Wet"));
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂ TE Wet"), "fCO₂ TE Wet")
      .matches(expectedValues.get("fCO₂ TE Wet"));
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ SST"), "pCO₂ SST")
      .matches(expectedValues.get("pCO₂ SST"));
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(expectedValues.get("fCO₂"));
  }
}
