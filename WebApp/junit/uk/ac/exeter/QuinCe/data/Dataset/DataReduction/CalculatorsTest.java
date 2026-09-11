package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;
import uk.ac.exeter.QuinCe.utils.BigDecimalWithUncertainty;
import uk.ac.exeter.QuinCe.utils.BigDecimalWithUncertaintyAssert;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertaintyAssert;

/**
 * Tests for the {@link Calculators} class.
 */
public class CalculatorsTest extends BaseTest {

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)}.
   */
  @Test
  public void calcPH2OTest() {
    DoubleWithUncertainty temp = new DoubleWithUncertainty(11.17D, 0.1F);
    DoubleWithUncertainty salinity = new DoubleWithUncertainty(34.185D, 0.05F);

    DoubleWithUncertainty result = Calculators.calcPH2O(salinity, temp);
    DoubleWithUncertaintyAssert.assertThat(result).matches(0.012847D, 0.0001F);
  }

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)} with {@code NaN}
   * salinity.
   */
  @Test
  public void calcPH2ONanSalinityTest() {
    DoubleWithUncertainty temp = new DoubleWithUncertainty(11.17D, 0.1F);
    DoubleWithUncertainty salinity = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);

    DoubleWithUncertainty result = Calculators.calcPH2O(salinity, temp);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)} with {@code NaN}
   * salinity.
   */
  @Test
  public void calcPH2ONanTempTest() {
    DoubleWithUncertainty temp = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);
    DoubleWithUncertainty salinity = new DoubleWithUncertainty(34.185D, 0.05F);

    DoubleWithUncertainty result = Calculators.calcPH2O(salinity, temp);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)}.
   */
  @Test
  public void calcPco2WetTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.5F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.2F);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016D, 0.0001F);

    DoubleWithUncertainty result = Calculators.calcpCO2TEWet(xCO2, pressure,
      pH2O);
    DoubleWithUncertaintyAssert.assertThat(result).matches(347.2683D, 0.5015F);
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} xCO₂ value.
   */
  @Test
  public void calcPco2WetNanXCO2Test() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.2F);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016D, 0.0001F);

    DoubleWithUncertainty result = Calculators.calcpCO2TEWet(xCO2, pressure,
      pH2O);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} pressure value.
   */
  @Test
  public void calcPco2WetNanPressureTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.5F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016D, 0.0001F);

    DoubleWithUncertainty result = Calculators.calcpCO2TEWet(xCO2, pressure,
      pH2O);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} pH₂O value.
   */
  @Test
  public void calcPco2WetNanPH2OTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.5F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.2F);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);

    DoubleWithUncertainty result = Calculators.calcpCO2TEWet(xCO2, pressure,
      pH2O);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)}.
   */
  @Test
  public void calcFCO2Test() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26D, 0.2F);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.1F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.15F);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82D,
      0.05F);

    DoubleWithUncertainty fCO2 = Calculators.calcfCO2(pCO2, xCO2, pressure,
      temperature);
    DoubleWithUncertaintyAssert.assertThat(fCO2).matches(345.9275D, 0.2F);
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} pCO₂ value.
   */
  @Test
  public void calcFCO2NaNpCO2Test() {
    DoubleWithUncertainty pCO2 = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.1F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.15F);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82D,
      0.05F);

    DoubleWithUncertainty fCO2 = Calculators.calcfCO2(pCO2, xCO2, pressure,
      temperature);
    DoubleWithUncertaintyAssert.assertThat(fCO2).nan();
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} xCO₂ value.
   */
  @Test
  public void calcFCO2NaNXCO2Test() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26D, 0.2F);
    DoubleWithUncertainty xCO2 = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.15F);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82D,
      0.05F);

    DoubleWithUncertainty fCO2 = Calculators.calcfCO2(pCO2, xCO2, pressure,
      temperature);
    DoubleWithUncertaintyAssert.assertThat(fCO2).nan();
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} pressure value.
   */
  @Test
  public void calcFCO2NaNPressureTest() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26D, 0.2F);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.1F);
    DoubleWithUncertainty pressure = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82D,
      0.05F);

    DoubleWithUncertainty fCO2 = Calculators.calcfCO2(pCO2, xCO2, pressure,
      temperature);
    DoubleWithUncertaintyAssert.assertThat(fCO2).nan();
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} temperature value.
   */
  @Test
  public void calcFCO2NaNTempTest() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26D, 0.2F);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43D, 0.1F);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32D, 0.15F);
    DoubleWithUncertainty temperature = DoubleWithUncertainty.NaN;

    DoubleWithUncertainty fCO2 = Calculators.calcfCO2(pCO2, xCO2, pressure,
      temperature);
    DoubleWithUncertaintyAssert.assertThat(fCO2).nan();
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a positive
   * value.
   */
  @Test
  public void kelvinPositiveTest() {
    DoubleWithUncertainty kelvin = Calculators
      .kelvin(new DoubleWithUncertainty(9.48, 0.01F));
    DoubleWithUncertaintyAssert.assertThat(kelvin).matches(282.63D, 0.01F);
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a zero value.
   */
  @Test
  public void kelvinZeroTest() {
    DoubleWithUncertainty kelvin = Calculators
      .kelvin(new DoubleWithUncertainty(0D, 0.1F));
    DoubleWithUncertaintyAssert.assertThat(kelvin).matches(273.15D, 0.1F);
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a negative
   * value.
   */
  @Test
  public void kelvinNegativeTest() {
    DoubleWithUncertainty kelvin = Calculators
      .kelvin(new DoubleWithUncertainty(-9.48D, 0.01F));
    DoubleWithUncertaintyAssert.assertThat(kelvin).matches(263.67D, 0.01F);
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a positive
   * result.
   */
  @Test
  public void celsiusPositiveTest() {
    DoubleWithUncertainty celsius = Calculators
      .celsius(new DoubleWithUncertainty(282.63D, 0.02F));
    DoubleWithUncertaintyAssert.assertThat(celsius).matches(9.48D, 0.02F);
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a zero result.
   */
  @Test
  public void celsiusZeroTest() {
    DoubleWithUncertainty celsius = Calculators
      .celsius(new DoubleWithUncertainty(273.15D, 0.02F));
    DoubleWithUncertaintyAssert.assertThat(celsius).matches(0D, 0.02F);
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a negative
   * result.
   */
  @Test
  public void celsiusNegativeTest() {
    DoubleWithUncertainty celsius = Calculators
      .celsius(new DoubleWithUncertainty(282.63D, 0.02F));
    DoubleWithUncertaintyAssert.assertThat(celsius).matches(9.48D, 0.02F);
  }

  /**
   * Test
   * {@link Calculators#calcCO2AtSST(DoubleWithUncertainty, DoubleWithUncertainty, DoubleWithUncertainty)}.
   */
  @Test
  public void calcCO2AtSSTTest() {
    DoubleWithUncertainty co2 = new DoubleWithUncertainty(402.43D, 0.8F);
    DoubleWithUncertainty eqt = new DoubleWithUncertainty(6.34D, 0.15F);
    DoubleWithUncertainty sst = new DoubleWithUncertainty(5.35D, 0.09F);

    DoubleWithUncertainty result = Calculators.calcCO2AtSST(co2, eqt, sst);
    DoubleWithUncertaintyAssert.assertThat(result).matches(385.9254D, 2.9569F);
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}.
   */
  @Test
  public void calcSeaLevelPressureTest() {
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1023.244D, 0.5F);
    DoubleWithUncertainty temp = new DoubleWithUncertainty(7.44D, 0.1F);
    Float height = 10.2F;

    DoubleWithUncertainty result = Calculators.calcSeaLevelPressure(pressure,
      temp, height);
    DoubleWithUncertaintyAssert.assertThat(result).matches(1024.5142D, 0.5F);
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}.
   */
  @Test
  public void calcSeaLevelPressureNaNPressureTest() {
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);
    DoubleWithUncertainty temp = new DoubleWithUncertainty(7.44D, 0.1F);
    Float height = 10.2F;

    DoubleWithUncertainty result = Calculators.calcSeaLevelPressure(pressure,
      temp, height);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}.
   */
  @Test
  public void calcSeaLevelNaNTempTest() {
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1023.244D, 0.5F);
    DoubleWithUncertainty temp = new DoubleWithUncertainty(Double.NaN,
      Float.NaN);
    Float height = 10.2F;

    DoubleWithUncertainty result = Calculators.calcSeaLevelPressure(pressure,
      temp, height);
    DoubleWithUncertaintyAssert.assertThat(result).nan();
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}
   * with a {@code null} height value.
   */
  @ParameterizedTest
  @NullSource
  @ValueSource(floats = { Float.NaN })
  public void calcSeaLevelPressureNoHeightTest(Float height) {
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1023.244D, 0.5F);
    DoubleWithUncertainty temp = new DoubleWithUncertainty(7.44D, 0.1F);

    DoubleWithUncertainty result = Calculators.calcSeaLevelPressure(pressure,
      temp, height);
    DoubleWithUncertaintyAssert.assertThat(result).matches(1023.244D, 0.5F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesTest() {

    double x0 = 26.533D;
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(8.328D, 0.2F);
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(15.685D, 0.1F);
    double targetX = 37.765D;

    DoubleWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    DoubleWithUncertaintyAssert.assertThat(result).matches(10.7288D, 0.1387F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesNullY0Test() {
    double x0 = 26.533D;
    DoubleWithUncertainty y0 = null;
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(15.685D, 0.1F);
    double targetX = 37.765D;

    DoubleWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    DoubleWithUncertaintyAssert.assertThat(result).matches(15.685D, 0.1F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesNaNY0Test() {
    double x0 = 26.533D;
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(Double.NaN, Float.NaN);
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(15.685D, 0.1F);
    double targetX = 37.765D;

    DoubleWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    DoubleWithUncertaintyAssert.assertThat(result).matches(15.685D, 0.1F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesNullY1Test() {
    double x0 = 26.533D;
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(8.328D, 0.2F);
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = null;
    double targetX = 37.765D;

    DoubleWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    DoubleWithUncertaintyAssert.assertThat(result).matches(8.328D, 0.2F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesNaNY1Test() {
    double x0 = 26.533D;
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(8.328D, 0.2F);
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(Double.NaN, Float.NaN);
    double targetX = 37.765D;

    DoubleWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    DoubleWithUncertaintyAssert.assertThat(result).matches(8.328D, 0.2F);
  }

  @Test
  public void interpolateBigDecimalsNullY0Test() {
    double x0 = 26.533D;
    BigDecimalWithUncertainty y0 = null;
    double x1 = 60.952D;
    BigDecimalWithUncertainty y1 = new BigDecimalWithUncertainty(
      new BigDecimal(15.685D), 0.1F);
    double targetX = 37.765D;

    BigDecimalWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    BigDecimalWithUncertaintyAssert.assertThat(result)
      .matches(new BigDecimal(15.685D), 0.1F);
  }

  @Test
  public void interpolateBigDecimalsNullY1Test() {
    double x0 = 26.533D;
    BigDecimalWithUncertainty y0 = new BigDecimalWithUncertainty(
      new BigDecimal(8.328D), 0.2F);
    double x1 = 60.952D;
    BigDecimalWithUncertainty y1 = null;
    double targetX = 37.765D;

    BigDecimalWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    BigDecimalWithUncertaintyAssert.assertThat(result)
      .matches(new BigDecimal(8.328D), 0.2F);
  }

  @Test
  public void interpolateBigDecimalsTest() {
    double x0 = 26.533D;
    BigDecimalWithUncertainty y0 = new BigDecimalWithUncertainty(
      new BigDecimal(8.328D), 0.2F);
    double x1 = 60.952D;
    BigDecimalWithUncertainty y1 = new BigDecimalWithUncertainty(
      new BigDecimal(15.685D), 0.1F);
    double targetX = 37.765D;

    BigDecimalWithUncertainty result = Calculators.interpolate(x0, y0, x1, y1,
      targetX);
    BigDecimalWithUncertaintyAssert.assertThat(result)
      .matches(new BigDecimal(10.7288D), 0.1387F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with {@code null} y values.
   */
  @Test
  public void interpolateTimesNullYsTest() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = null;
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = null;
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    assertNull(Calculators.interpolate(time0, y0, time1, y1, targetTime));
  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with a {@code null} first y value.
   */
  @Test
  public void interpolateTimesNullY0Test() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = null;
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(50.602D, 0.07F);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    DoubleWithUncertainty result = Calculators.interpolate(time0, y0, time1, y1,
      targetTime);
    DoubleWithUncertaintyAssert.assertThat(result).matches(50.602D, 0.07F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with a {@code null} second y value.
   */
  @Test
  public void interpolateTimesNullY1Test() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(5.666D, 0.12F);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = null;
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    DoubleWithUncertainty result = Calculators.interpolate(time0, y0, time1, y1,
      targetTime);
    DoubleWithUncertaintyAssert.assertThat(result).matches(5.666D, 0.12F);

  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with a {@code null} first y value.
   */
  @Test
  public void interpolateTimesNaNY0Test() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(Double.NaN, Float.NaN);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(50.602D, 0.07F);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    DoubleWithUncertainty result = Calculators.interpolate(time0, y0, time1, y1,
      targetTime);
    DoubleWithUncertaintyAssert.assertThat(result).matches(50.602D, 0.07F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with a {@code null} second y value.
   */
  @Test
  public void interpolateTimesNaNY1Test() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(5.666D, 0.12F);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(Double.NaN, Float.NaN);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    DoubleWithUncertainty result = Calculators.interpolate(time0, y0, time1, y1,
      targetTime);
    DoubleWithUncertaintyAssert.assertThat(result).matches(5.666D, 0.12F);

  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with all values.
   */
  @Test
  public void interpolateTimesDoublesTest() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(5.666D, 0.12F);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(50.602D, 0.07F);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    DoubleWithUncertainty result = Calculators.interpolate(time0, y0, time1, y1,
      targetTime);
    DoubleWithUncertaintyAssert.assertThat(result).matches(7.0276D, 0.1163F);
  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with all values.
   */
  @Test
  public void interpolateTimesBigDecimalsTest() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    BigDecimalWithUncertainty y0 = new BigDecimalWithUncertainty(
      new BigDecimal(5.666D), 0.12F);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    BigDecimalWithUncertainty y1 = new BigDecimalWithUncertainty(
      new BigDecimal(50.602D), 0.07F);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    BigDecimalWithUncertainty result = Calculators.interpolate(time0, y0, time1,
      y1, targetTime);
    BigDecimalWithUncertaintyAssert.assertThat(result)
      .matches(new BigDecimal(7.0276D), 0.1163F);
  }
}
