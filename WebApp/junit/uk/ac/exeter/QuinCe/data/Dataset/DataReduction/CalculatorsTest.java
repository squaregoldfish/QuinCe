package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

/**
 * Tests for the {@link Calculators} class.
 */
public class CalculatorsTest extends BaseTest {

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)}.
   */
  @Test
  public void calcPH2OTest() {
    DoubleWithUncertainty temp = new DoubleWithUncertainty(11.17);
    DoubleWithUncertainty salinity = new DoubleWithUncertainty(34.18548);
    DoubleWithUncertainty targetpH2O = new DoubleWithUncertainty(0.012847);

    assertEquals(targetpH2O.value(),
      Calculators.calcPH2O(salinity, temp).value(), 0.000001);

    assertTrue(false, "Uncertianty");
  }

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)} with {@code NaN}
   * salinity.
   */
  @Test
  public void calcPH2ONanSalinityTest() {
    assertEquals(DoubleWithUncertainty.NaN, Calculators
      .calcPH2O(DoubleWithUncertainty.NaN, new DoubleWithUncertainty(11.17)));

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcPH2O(Double, Double)} with {@code NaN}
   * salinity.
   */
  @Test
  public void calcPH2ONanTempTest() {
    assertEquals(DoubleWithUncertainty.NaN, Calculators
      .calcPH2O(new DoubleWithUncertainty(35.01), DoubleWithUncertainty.NaN));

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)}.
   */
  @Test
  public void calcPco2WetTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016);

    assertEquals(347.26826,
      Calculators.calcpCO2TEWet(xCO2, pressure, pH2O).value(), 0.00001);

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} xCO₂ value.
   */
  @Test
  public void calcPco2WetNanXCO2Test() {
    DoubleWithUncertainty xCO2 = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016);

    assertEquals(Double.NaN,
      Calculators.calcpCO2TEWet(xCO2, pressure, pH2O).value(), 0.00001);

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} pressure value.
   */
  @Test
  public void calcPco2WetNanPressureTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty pH2O = new DoubleWithUncertainty(0.016);

    assertEquals(Double.NaN,
      Calculators.calcpCO2TEWet(xCO2, pressure, pH2O).value(), 0.00001);

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcpCO2TEWet(Double, Double, Double)} with a
   * {@code NaN} pH₂O value.
   */
  @Test
  public void calcPco2WetNanPH2OTest() {
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty pH2O = DoubleWithUncertainty.NaN;

    assertEquals(Double.NaN,
      Calculators.calcpCO2TEWet(xCO2, pressure, pH2O).value(), 0.00001);

    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)}.
   */
  @Test
  public void calcFCO2Test() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82);

    assertEquals(345.92746,
      Calculators.calcfCO2(pCO2, xCO2, pressure, temperature).value(), 0.0001);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} pCO₂ value.
   */
  @Test
  public void calcFCO2NaNpCO2Test() {
    DoubleWithUncertainty pCO2 = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82);

    assertEquals(Double.NaN,
      Calculators.calcfCO2(pCO2, xCO2, pressure, temperature).value(), 0.0001);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} xCO₂ value.
   */
  @Test
  public void calcFCO2NaNXCO2Test() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26);
    DoubleWithUncertainty xCO2 = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82);

    assertEquals(Double.NaN,
      Calculators.calcfCO2(pCO2, xCO2, pressure, temperature).value(), 0.0001);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} pressure value.
   */
  @Test
  public void calcFCO2NaNPressureTest() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = DoubleWithUncertainty.NaN;
    DoubleWithUncertainty temperature = new DoubleWithUncertainty(10.82);

    assertEquals(Double.NaN,
      Calculators.calcfCO2(pCO2, xCO2, pressure, temperature).value(), 0.0001);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#calcfCO2(Double, Double, Double, Double)} with a
   * {@code NaN} temperature value.
   */
  @Test
  public void calcFCO2NaNTempTest() {
    DoubleWithUncertainty pCO2 = new DoubleWithUncertainty(347.26);
    DoubleWithUncertainty xCO2 = new DoubleWithUncertainty(350.43);
    DoubleWithUncertainty pressure = new DoubleWithUncertainty(1020.32);
    DoubleWithUncertainty temperature = DoubleWithUncertainty.NaN;

    assertEquals(Double.NaN,
      Calculators.calcfCO2(pCO2, xCO2, pressure, temperature).value(), 0.0001);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a positive
   * value.
   */
  @Test
  public void kelvinPositiveTest() {
    assertEquals(282.63D,
      Calculators.kelvin(new DoubleWithUncertainty(9.48)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a zero value.
   */
  @Test
  public void kelvinZeroTest() {
    assertEquals(273.15D,
      Calculators.kelvin(new DoubleWithUncertainty(0D)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#kelvin(DoubleWithUncertainty)} with a negative
   * value.
   */
  @Test
  public void kelvinNegativeTest() {
    assertEquals(263.67D,
      Calculators.kelvin(new DoubleWithUncertainty(-9.48D)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a positive
   * result.
   */
  @Test
  public void celsiusPositiveTest() {
    assertEquals(9.48D,
      Calculators.celsius(new DoubleWithUncertainty(282.63D)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a zero result.
   */
  @Test
  public void celsiusZeroTest() {
    assertEquals(0D,
      Calculators.celsius(new DoubleWithUncertainty(273.15D)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test {@link Calculators#celsius(DoubleWithUncertainty)} with a negative
   * result.
   */
  @Test
  public void celsiusNegativeTest() {
    assertEquals(-9.48D,
      Calculators.celsius(new DoubleWithUncertainty(263.67D)).value(), 0.01);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test
   * {@link Calculators#calcCO2AtSST(DoubleWithUncertainty, DoubleWithUncertainty, DoubleWithUncertainty)}.
   */
  @Test
  public void calcCO2AtSSTTest() {
    assertEquals(385.9254D,
      Calculators
        .calcCO2AtSST(new DoubleWithUncertainty(402.43D),
          new DoubleWithUncertainty(6.34D), new DoubleWithUncertainty(5.35D))
        .value(),
      0.0001D);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}.
   */
  @Test
  public void calcSeaLevelPressureTest() {
    assertEquals(1024.5142D,
      Calculators.calcSeaLevelPressure(new DoubleWithUncertainty(1023.244D),
        new DoubleWithUncertainty(7.44D), 10.2F).value(),
      0.0001D);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test
   * {@link Calculators#calcSeaLevelPressure(DoubleWithUncertainty, DoubleWithUncertainty, Float)}
   * with a {@code null} height value.
   */
  @Test
  public void calcSeaLevelPressureNoHeightTest() {
    assertEquals(1023.244D,
      Calculators.calcSeaLevelPressure(new DoubleWithUncertainty(1023.244D),
        new DoubleWithUncertainty(7.44D), null).value(),
      0.0001D);
    assertTrue(false, "Uncertainty");
  }

  /**
   * Test
   * {@link Calculators#interpolate(double, double, double, double, double)}.
   */
  @Test
  public void interpolateDoublesTest() {

    double x0 = 26.533D;
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(8.328D);
    double x1 = 60.952D;
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(15.685D);
    double targetX = 37.765D;

    assertEquals(10.7288D,
      Calculators.interpolate(x0, y0, x1, y1, targetX).value(), 0.0001D);

    assertTrue(false, "Uncertainty");
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
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(50.602D);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    assertEquals(50.602D,
      Calculators.interpolate(time0, y0, time1, y1, targetTime).value(),
      0.0001D);
    assertTrue(false, "Uncertainty");

  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with a {@code null} second y value.
   */
  @Test
  public void interpolateTimesNullY1Test() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(5.666D);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = null;
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    assertEquals(5.666D,
      Calculators.interpolate(time0, y0, time1, y1, targetTime).value(),
      0.0001D);
    assertTrue(false, "Uncertainty");

  }

  /**
   * Test
   * {@link Calculators#interpolate(LocalDateTime, Double, LocalDateTime, Double, LocalDateTime)}
   * with all values.
   */
  @Test
  public void interpolateTimesTest() {
    LocalDateTime time0 = LocalDateTime.of(2020, 1, 1, 12, 10, 00);
    DoubleWithUncertainty y0 = new DoubleWithUncertainty(5.666D);
    LocalDateTime time1 = LocalDateTime.of(2020, 1, 1, 12, 43, 00);
    DoubleWithUncertainty y1 = new DoubleWithUncertainty(50.602D);
    LocalDateTime targetTime = LocalDateTime.of(2020, 1, 1, 12, 11, 00);

    assertEquals(7.0276D,
      Calculators.interpolate(time0, y0, time1, y1, targetTime).value(),
      0.0001D);
    assertTrue(false, "Uncertainty");

  }
}
