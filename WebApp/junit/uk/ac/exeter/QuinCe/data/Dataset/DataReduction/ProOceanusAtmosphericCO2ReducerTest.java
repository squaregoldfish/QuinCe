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

public class ProOceanusAtmosphericCO2ReducerTest extends DataReducerTest {

  @FlywayTest
  @Test
  public void testReduction() throws Exception {

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = Mockito.mock(Variable.class);
    Mockito.when(variable.getId()).thenReturn(1L);

    // Initialise the reducer
    ProOceanusAtmosphericCO2Reducer reducer = new ProOceanusAtmosphericCO2Reducer(
      variable, new HashMap<String, Properties>(), null);

    MeasurementValue airTemp = makeMeasurementValue("Air Temperature",
      new DoubleWithUncertainty(14.755D, 0.2F));

    MeasurementValue cellGasPressure = makeMeasurementValue("Cell Gas Pressure",
      new DoubleWithUncertainty(1022.55D, 0.5F));

    MeasurementValue humidityPressure = makeMeasurementValue(
      "Humidity Pressure", new DoubleWithUncertainty(14.67D, 0.1F));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (wet, no standards)",
      new DoubleWithUncertainty(398.419D, 0.7F));

    Measurement measurement = makeMeasurement(airTemp, cellGasPressure,
      humidityPressure, xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("xCO₂"), "xCO₂")
      .matches(404.21811D, 0.7113F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂"), "pCO₂")
      .matches(402.07584D, 0.7333F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(400.60542D, 0.7351F);
  }
}
