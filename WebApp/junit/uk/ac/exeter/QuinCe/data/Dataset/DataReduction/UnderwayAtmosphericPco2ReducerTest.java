package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
      new DoubleWithUncertainty(15.453D));

    MeasurementValue salinity = makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(35.224D));

    MeasurementValue atmPressure = makeMeasurementValue("Atmospheric Pressure",
      new DoubleWithUncertainty(1020.03D));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (with standards)",
      new DoubleWithUncertainty(402.043D));

    Measurement measurement = makeMeasurement(waterTemp, salinity, atmPressure,
      xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    assertEquals(1021.236915D,
      record.getCalculationValue("Sea Level Pressure").value(), 0.0001);
    assertEquals(0.01698133874D, record.getCalculationValue("pH₂O").value(),
      0.0001);
    assertEquals(398.384864382D, record.getCalculationValue("pCO₂").value(),
      0.0001);
    assertEquals(396.942602093D, record.getCalculationValue("fCO₂").value(),
      0.0001);
    assertTrue(false, "Uncertainty");
  }
}
