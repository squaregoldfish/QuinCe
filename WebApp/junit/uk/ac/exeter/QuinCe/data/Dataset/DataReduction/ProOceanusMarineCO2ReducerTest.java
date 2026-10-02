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

public class ProOceanusMarineCO2ReducerTest extends DataReducerTest {

  @FlywayTest
  @Test
  public void testReduction() throws Exception {

    // Mock objects
    Instrument instrument = Mockito.mock(Instrument.class);
    Mockito.when(instrument.getId()).thenReturn(1L);

    Variable variable = Mockito.mock(Variable.class);
    Mockito.when(variable.getId()).thenReturn(1L);

    // Initialise the reducer
    ProOceanusMarineCO2Reducer reducer = new ProOceanusMarineCO2Reducer(
      variable, new HashMap<String, Properties>(), null);

    MeasurementValue waterTemp = makeMeasurementValue("Water Temperature",
      new DoubleWithUncertainty(10.777D, 0.07F));

    MeasurementValue cellGasPressure = makeMeasurementValue("Cell Gas Pressure",
      new DoubleWithUncertainty(1014.81D, 0.1F));

    MeasurementValue xco2 = makeMeasurementValue("xCO₂ (wet, no standards)",
      new DoubleWithUncertainty(393.722D, 0.6F));

    Measurement measurement = makeMeasurement(waterTemp, cellGasPressure, xco2);

    // Make a record to work with
    DataReductionRecord record = new DataReductionRecord(measurement, variable,
      flagScheme, reducer.getCalculationParameterNames());

    reducer.doCalculation(instrument, measurement, record,
      getDataSource().getConnection());

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("pCO₂ SST"), "pCO₂ SST")
      .matches(394.32817D, 0.6022F);

    DoubleWithUncertaintyAssert
      .assertThat(record.getCalculationValue("fCO₂"), "fCO₂")
      .matches(392.82228D, 0.6005F);
  }
}
