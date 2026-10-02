package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.ac.exeter.QuinCe.data.Dataset.CoordinateException;
import uk.ac.exeter.QuinCe.data.Dataset.Measurement;
import uk.ac.exeter.QuinCe.data.Dataset.MeasurementValue;
import uk.ac.exeter.QuinCe.data.Instrument.Instrument;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorTypeNotFoundException;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.Variable;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;

/**
 * Test for the {@link UnderwayMarinePco2Reducer}.
 *
 * <p>
 * Note that this only tests the pCO₂/fCO₂ and intermediate calculations, and
 * not things like calibrating CO₂ and equilibrator pressure - these are
 * performed outside the reducer and therefore tested in the relevant places.
 * </p>
 */
public class UnderwayMarinePco2ReducerTest extends DataReducerTest {

  @FlywayTest
  @Test
  public void testReduction() throws Exception {

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = Mockito.mock(Variable.class);
    Mockito.when(variable.getId()).thenReturn(1L);

    // Initialise the reducer
    UnderwayMarinePco2Reducer reducer = new UnderwayMarinePco2Reducer(variable,
      new HashMap<String, Properties>(), null);

    MeasurementValue waterTemp = makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(11.912D, 0.01F));

    MeasurementValue salinity = makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(35.224D, 0.04F));

    MeasurementValue eqTemp = makeMeasurementValue("Equilibrator Temperature",
      new DoubleWithUncertainty(12.37D, 0.1F));

    MeasurementValue eqPressure = makeMeasurementValue("Equilibrator Pressure",
      new DoubleWithUncertainty(999.23D, 0.12F));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (with standards)",
      new DoubleWithUncertainty(374.977D, 0.4F));

    Measurement measurement = makeMeasurement(waterTemp, salinity, eqTemp,
      eqPressure, xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    // Check the calculated values in the record
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("ΔT"), "ΔT")
      .matches(0.458D, 0.1005F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pH₂O"), "pH₂O")
      .matches(0.01389918297D, 0.0001F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ TE Wet"), "pCO₂ TE Wet")
      .matches(364.576695236D, 0.3939F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂ TE Wet"), "fCO₂ TE Wet")
      .matches(363.233567921D, 0.3941F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ SST"), "pCO₂ SST")
      .matches(357.5815834D, 1.5684F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(356.2642266D, 1.5631F);
  }

  @FlywayTest
  @Test
  public void largeDeltaTTest() throws SensorTypeNotFoundException,
    DataReductionException, SQLException, CoordinateException {

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = Mockito.mock(Variable.class);
    Mockito.when(variable.getId()).thenReturn(1L);

    UnderwayMarinePco2Reducer reducer = new UnderwayMarinePco2Reducer(variable,
      new HashMap<String, Properties>(), null);

    MeasurementValue waterTemp = makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(11.912D, 0.01F));

    MeasurementValue salinity = makeMeasurementValue("Salinity",
      new DoubleWithUncertainty(35.224D, 0.04F));

    MeasurementValue eqTemp = makeMeasurementValue("Equilibrator Temperature",
      new DoubleWithUncertainty(1000D, 0.1F));

    MeasurementValue eqPressure = makeMeasurementValue("Equilibrator Pressure",
      new DoubleWithUncertainty(999.23D, 0.12F));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (with standards)",
      new DoubleWithUncertainty(374.977D, 0.4F));

    Measurement measurement = makeMeasurement(waterTemp, salinity, eqTemp,
      eqPressure, xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    // Check the calculated values in the record
    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("ΔT"), "ΔT")
      .matches(988.088D, 0.1005F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pH₂O"), "pH₂O")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ TE Wet"), "pCO₂ TE Wet")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂ TE Wet"), "fCO₂ TE Wet")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ SST"), "pCO₂ SST")
      .matches(Double.NaN, 0F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(Double.NaN, 0F);
  }
}
