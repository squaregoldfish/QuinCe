package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.util.HashMap;
import java.util.Properties;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;

/**
 * Test for the {@link UnderwayAtmosphericPco2Reducer}.
 *
 * <p>
 * Note that this only tests the pCO₂/fCO₂ and intermediate calculations, and
 * not things like calibrating CO₂ and equilibrator pressure - these are
 * performed outside the reducer and therefore tested in the relevant places.
 * </p>
 */
public class UnderwayAtmosphericPco2ReducerTest extends DataReducerTest {

  private static final String VAR_NAME = "Underway Atmospheric pCO₂";

  @FlywayTest
  @Test
  public void testReduction() throws Exception {
    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = Mockito.mock(Variable.class);
    Mockito.when(variable.getId()).thenReturn(1L);
    Mockito.when(variable.getName()).thenReturn(VAR_NAME);

    Properties props = new Properties();
    props.put("atm_pres_sensor_height", "10");
    HashMap<String, Properties> reducerProps = new HashMap<String, Properties>();
    reducerProps.put(VAR_NAME, props);

    UnderwayAtmosphericPco2Reducer reducer = new UnderwayAtmosphericPco2Reducer(
      variable, reducerProps, null);

    MeasurementValue waterTemp = makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(15.453D, 0.01F));

    MeasurementValue salinity = makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(35.224D, 0.02F));

    MeasurementValue atmPressure = makeMeasurementValue("Atmospheric Pressure",
      new DoubleWithUncertainty(1020.03D, 0.1F));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (with standards)",
      new DoubleWithUncertainty(402.043D, 0.4F));

    Measurement measurement = makeMeasurement(waterTemp, salinity, atmPressure,
      xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("Sea Level Pressure"),
        "Sea Level Pressure")
      .matches(1021.236915D, 0.1F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pH₂O"), "pH₂O")
      .matches(0.01698133874D, 0.00001F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂"), "pCO₂")
      .matches(398.384864382D, 0.3984F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(396.942602093D, 0.397F);
  }
}
