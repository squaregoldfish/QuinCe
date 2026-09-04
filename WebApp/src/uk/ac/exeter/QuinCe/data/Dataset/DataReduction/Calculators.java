package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

/**
 * Provides static methods for common calculations in data reduction.
 */
public class Calculators {

  /**
   * The conversion factor from Pascals to Atmospheres.
   */
  private static final DoubleWithUncertainty PASCALS_TO_ATMOSPHERES = new DoubleWithUncertainty(
    0.00000986923266716013, 0F);

  /**
   * The molar mass of air.
   */
  private static final DoubleWithUncertainty MOLAR_MASS_AIR = new DoubleWithUncertainty(
    28.97e-3);

  /**
   * Kelvin offset.
   */
  public static final DoubleWithUncertainty KELVIN_OFFSET = new DoubleWithUncertainty(
    273.15);

  /**
   * Convert a temperature in °C to °K.
   *
   * @param celsius
   *          Celsius temperature.
   * @return Kelvin temperature.
   */
  public static DoubleWithUncertainty kelvin(DoubleWithUncertainty celsius) {
    return celsius.add(KELVIN_OFFSET);
  }

  /**
   * Convert a temperature in °K to °C.
   *
   * @param kelvin
   *          Kelvin temperature.
   * @return Celsius temperature.
   */
  public static DoubleWithUncertainty celsius(DoubleWithUncertainty kelvin) {
    return kelvin.subtract(KELVIN_OFFSET);
  }

  /**
   * Convert a pressure in hPa it atmospheres.
   *
   * @param hPa
   *          Pressure in hPa.
   * @return Pressure in atmospheres.
   */
  public static DoubleWithUncertainty hPaToAtmospheres(
    DoubleWithUncertainty hPa) {
    return hPa.multiply(DoubleWithUncertainty.HUNDRED)
      .multiply(PASCALS_TO_ATMOSPHERES);
  }

  /**
   * Converts pCO<sub>2</sub> to fCO<sub>2</sub>.
   *
   * @param pco2
   *          pCO<sub>2</sub> at target temperature.
   * @param xCO2InGas
   *          The calibrated, dried xCO<sub>2</sub> value.
   * @param pressure
   *          The pressure in hPa.
   * @param temperature
   *          The temperature in °C
   * @return The fCO<sub>2</sub> value.
   */
  public static DoubleWithUncertainty calcfCO2(DoubleWithUncertainty pco2,
    DoubleWithUncertainty xCO2InGas, DoubleWithUncertainty pressure,
    DoubleWithUncertainty temperature) {

    DoubleWithUncertainty kelvin = Calculators.kelvin(temperature);

    // B_a = -1636.75
    // B_b = 12.0408 * kelvin
    // B_c = 0.0327957 * kelvin²
    // B_d = (2.16528e-5) * kelvin³
    // B = B_a + B_b - B_c + B_d

    DoubleWithUncertainty B_a = new DoubleWithUncertainty(-1646.75);
    DoubleWithUncertainty B_b = kelvin.multiply(12.0408);
    DoubleWithUncertainty B_c = kelvin.pow(2).multiply(0.0327957);
    DoubleWithUncertainty B_d = kelvin.pow(3).multiply(2.16528 * 1e-5);

    DoubleWithUncertainty B = B_a.add(B_b).subtract(B_c).add(B_d);

    DoubleWithUncertainty delta = new DoubleWithUncertainty(57.7)
      .subtract(kelvin.multiply(0.118));

    // fCO2_a = (1 - xCO2 * 1e-6)²
    // fCO2_b = 2 * fCO2_a * delta
    // fCO2_c = B + fCO2_b
    // fCO2_d = fCO2_c * hPaToAtm(pressure)
    // fCO2_e = 82.0575 * kelvin
    // fCO2_f = fCO2_d / fCO2_d
    // fCO2 = exp(fCO2_f)

    DoubleWithUncertainty fCO2_a = DoubleWithUncertainty.ONE
      .subtract(xCO2InGas.multiply(1e-6)).pow(2);
    DoubleWithUncertainty fCO2_b = fCO2_a.multiply(delta).multiply(2);
    DoubleWithUncertainty fCO2_c = B.add(fCO2_b);
    DoubleWithUncertainty fCO2_d = fCO2_c.multiply(hPaToAtmospheres(pressure));
    DoubleWithUncertainty fCO2_e = kelvin.multiply(82.0575);
    DoubleWithUncertainty fCO2_f = fCO2_d.divide(fCO2_e);
    return fCO2_f.exp();
  }

  /**
   * Calculates pCO<sub>2</sub> in water from xCO<sub>2</sub> measured in a gas
   * analyser.
   *
   * @param xCO2
   *          The dry, calibrated xCO<sub>2</sub> value.
   * @param pressure
   *          The pressure of equilibration.
   * @param pH2O
   *          The water vapour pressure.
   * @return pCO<sub>2</sub> in water.
   */
  public static DoubleWithUncertainty calcpCO2TEWet(DoubleWithUncertainty xCO2,
    DoubleWithUncertainty pressure, DoubleWithUncertainty pH2O) {
    return xCO2.multiply(hPaToAtmospheres(pressure).subtract(pH2O));
  }

  /**
   * Calculates the water vapour pressure (pH<sub>2</sub>O). From Weiss and
   * Price (1980),
   * <a href="https://doi.org/10.1016/0304-4203(80)90024-9" target=
   * "_blank">doi: 10.1016/0304-4203(80)90024-9</a>.
   *
   * @param salinity
   *          Salinity.
   * @param temperature
   *          Temperature in °C.
   * @return The calculated pH<sub>2</sub>O value.
   */
  public static DoubleWithUncertainty calcPH2O(DoubleWithUncertainty salinity,
    DoubleWithUncertainty temperature) {

    DoubleWithUncertainty kelvin = Calculators.kelvin(temperature);

    // pH2O_a = 24.4543
    // pH2O_b = 67.4509 * (100 / kelvin)
    // pH2O_c = 4.8489 * ln(kelvin/100)
    // pH2O_d = 0.000544 * salinity
    // pH2O = exp(pH2O_a - pH2O_b - pH2O_c - pH2O_d)

    DoubleWithUncertainty pH2O_a = new DoubleWithUncertainty(24.4543);
    DoubleWithUncertainty pH2O_b = DoubleWithUncertainty.HUNDRED.divide(kelvin)
      .multiply(67.4509);
    DoubleWithUncertainty pH2O_c = kelvin.divide(DoubleWithUncertainty.HUNDRED)
      .log().multiply(4.8489);
    DoubleWithUncertainty pH2O_d = salinity.multiply(0.000544);

    return pH2O_a.subtract(pH2O_b).subtract(pH2O_c).subtract(pH2O_d).exp();
  }

  /**
   * Adjust a measured pressure to sea level.
   *
   * <p>
   * If the supplied {@code sensorHeight} is {@code null}, no correction is made
   * and the original value is returned.
   * </p>
   *
   * @param measuredPressure
   *          The measured pressure.
   * @param temperature
   *          The temperature at which the pressure was measured.
   * @param sensorHeight
   *          The height of the sensor.
   * @return The adjusted pressure.
   */
  public static DoubleWithUncertainty calcSeaLevelPressure(
    DoubleWithUncertainty measuredPressure, DoubleWithUncertainty temperature,
    Float sensorHeight) {

    DoubleWithUncertainty result = measuredPressure;

    if (null != sensorHeight) {

      DoubleWithUncertainty top = measuredPressure.multiply(MOLAR_MASS_AIR);
      DoubleWithUncertainty bottom = kelvin(temperature).multiply(8.314)
        .multiply(9.8).multiply(sensorHeight);

      result = measuredPressure.add(top.divide(bottom));
    }

    return result;
  }

  /**
   * Perform a linear interpolation between two values taken at different times,
   * giving a value at the specified target time.
   *
   * <p>
   * If either of the {@code y} values is {@code null}, the other is returned.
   * If both are {@code null}, {@code null} is returned.
   * </p>
   *
   * <p>
   * The method will extrapolate the target timestamp if it is beyond the
   * reference timestamps.
   * </p>
   *
   * @param time0
   *          The first reference timestamp.
   * @param y0
   *          The first reference y value.
   * @param time1
   *          The second reference timestamp.
   * @param y1
   *          The second reference y value.
   * @param targetTime
   *          The target timestamp for which a value must be calculated.
   * @return The interpolated y value at the target timestamp.
   */
  public static DoubleWithUncertainty interpolate(LocalDateTime time0,
    DoubleWithUncertainty y0, LocalDateTime time1, DoubleWithUncertainty y1,
    LocalDateTime targetTime) {

    DoubleWithUncertainty result = null;

    if (null != y0 && null != y1) {
      double x0 = DateTimeUtils.dateToLong(time0);
      double x1 = DateTimeUtils.dateToLong(time1);
      double target = DateTimeUtils.dateToLong(targetTime);

      double interpolatedValue = interpolate(x0, y0.value(), x1, y1.value(),
        target);
      float interpolatedUncertainty = interpolateUncertainty(x0,
        y0.uncertainty(), x1, y1.uncertainty(), target);
      result = new DoubleWithUncertainty(interpolatedValue,
        interpolatedUncertainty);
    } else if (null != y0) {
      result = y0;
    } else if (null != y1) {
      result = y1;
    }

    return result;
  }

  /**
   * Perform a linear interpolation between two pairs of {@code x}/{@code y}
   * values, giving a value at the specified target {@code x} value.
   *
   * <p>
   * If either of the {@code y} values is {@code null}, the other is returned.
   * If both are {@code null}, {@code null} is returned.
   * </p>
   *
   * <p>
   * The method will extrapolate the target {@code x} if it is beyond the
   * reference values.
   * </p>
   *
   * @param x0
   *          The first reference x value.
   * @param y0
   *          The first reference y value.
   * @param x1
   *          The second reference x value.
   * @param y1
   *          The second reference y value.
   * @param target
   *          The target x value for which a value must be calculated.
   * @return The interpolated y value at the target x value.
   */
  public static DoubleWithUncertainty interpolate(Double x0,
    DoubleWithUncertainty y0, Double x1, DoubleWithUncertainty y1,
    Double target) {

    DoubleWithUncertainty result = null;

    if (!DoubleWithUncertainty.isNaN(y0) && !DoubleWithUncertainty.isNaN(y1)) {
      double interpolatedValue = interpolate(x0, y0.value(), x1, y1.value(),
        target);
      float interpolatedUncertainty = interpolateUncertainty(x0,
        y0.uncertainty(), x1, y1.uncertainty(), target);
      result = new DoubleWithUncertainty(interpolatedValue,
        interpolatedUncertainty);
    } else if (null != y0) {
      result = y0;
    } else if (null != y1) {
      result = y1;
    }

    return result;
  }

  /**
   * Perform a linear interpolation between two points to produce a value at a
   * third target point.
   *
   * <p>
   * Algorithm from DOI 10.1007/s10765-016-2174-6 eq 14.
   * </p>
   *
   * @param x0
   *          The first reference x value.
   * @param y0
   *          The first reference y value.
   * @param x1
   *          The second reference x value.
   * @param y1
   *          The second reference y value.
   * @param target
   *          The target x value for which a value must be calculated.
   * @return The interpolated y value at the target x value.
   */
  private static double interpolate(double x0, double y0, double x1, double y1,
    double target) {

    return y0 * ((target - x1) / (x0 - x1)) + y1 * ((target - x0) / (x1 - x0));
  }

  /**
   * Calculate the uncertainty for a linearly interpolation value (per
   * {@link #interpolate(double, double, double, double, double)}) based on the
   * uncertainties of the values being interpolated.
   *
   * <p>
   * Algorithm from DOI 10.1007/s10765-016-2174-6 eq 15.
   * </p>
   *
   * @param x0
   *          The first reference x value.
   * @param u0
   *          The uncertainty of the first reference y value.
   * @param x1
   *          The second reference x value.
   * @param u1
   *          The uncertainty of the second reference y value.
   * @param target
   *          The target x value.
   * @return The uncertainty of the interpolated y value.
   */
  private static float interpolateUncertainty(double x0, float u0, double x1,
    double u1, double target) {

    return (float) ((float) Math
      .sqrt(Math.pow((target - x1) / (x0 - x1), 2) * Math.pow(u0, 2))
      + (Math.pow((target - x0) / (x1 - x0), 2) * Math.pow(u1, 2)));
  }

  /**
   * Perform a linear interpolation between two points to produce a value at a
   * third target point.
   *
   * <p>
   * Algorithm from DOI 10.1007/s10765-016-2174-6 eq 14.
   * </p>
   *
   * @param x0
   *          The first reference x value.
   * @param y0
   *          The first reference y value.
   * @param x1
   *          The second reference x value.
   * @param y1
   *          The second reference y value.
   * @param target
   *          The target x value for which a value must be calculated.
   * @return The interpolated y value at the target x value.
   */
  public static BigDecimal interpolate(BigDecimal x0, BigDecimal y0,
    BigDecimal x1, BigDecimal y1, BigDecimal target) {

    BigDecimal result = null;

    boolean priorNull = null == x0 || null == y0;
    boolean postNull = null == x1 || null == y1;

    if (!priorNull && !postNull) {

      BigDecimal targetMinusX1 = target.subtract(x1);
      BigDecimal X0minusX1 = x0.subtract(x1);
      BigDecimal leftDivision = targetMinusX1.divide(X0minusX1);
      BigDecimal leftSide = y0.multiply(leftDivision);

      BigDecimal targetMinusX0 = target.subtract(x0);
      BigDecimal X1minusX0 = x1.subtract(x0);
      BigDecimal rightDivision = targetMinusX0.divide(X1minusX0);
      BigDecimal rightSide = y1.multiply(rightDivision);

      result = leftSide.add(rightSide);
    } else if (!priorNull) {
      result = y0;
    } else if (!postNull) {
      result = y1;
    }

    return result;
  }

  /**
   * Perform a linear interpolation between two points to produce a value at a
   * third target point.
   *
   * @param prior
   *          The first reference x/y value.
   * @param post
   *          The second reference x/y value.
   * @param x
   *          The target x value for which a value must be calculated.
   * @return The interpolated y value at the target x value.
   */
  public static Double interpolate(Map.Entry<Double, Double> prior,
    Map.Entry<Double, Double> post, Double x) {

    Double result = null;

    if (!isNull(prior) && !isNull(post)) {
      double x0 = prior.getKey();
      double y0 = prior.getValue();
      double x1 = post.getKey();
      double y1 = post.getValue();
      result = Calculators.interpolate(x0, y0, x1, y1, x.doubleValue());
    } else if (!isNull(prior)) {
      result = prior.getValue();
    } else if (!isNull(post)) {
      result = post.getValue();
    }

    return result;
  }

  /**
   * Determine whether a {@link Map.Entry} of {@link Double} objects is
   * {@code null}, or if either the key or value is {@code null} or {@code NaN}.
   *
   * @param mapEntry
   *          The entry to check.
   * @return {@code true} if any aspect of the entry is {@code null};
   *         {@code false} otherwise.
   */
  private static boolean isNull(Map.Entry<Double, Double> mapEntry) {

    boolean result = false;

    if (null == mapEntry) {
      result = true;
    } else if (null == mapEntry.getKey() || mapEntry.getKey().isNaN()) {
      result = true;
    } else if (null == mapEntry.getValue() || mapEntry.getValue().isNaN()) {
      result = true;
    }

    return result;
  }

  /**
   * Calculates pCO<sub>2</sub> at the water (sea surface) temperature. From
   * Takahashi et al. (2009),
   * <a href="https://doi.org/10.1016/j.dsr2.2008.12.009" target="_blank">doi:
   * 10.1016/j.dsr2.2008.12.009</a>.
   *
   * @param co2AtEquilibrator
   *          The pCO<sub>2</sub> at equilibrator temperature.
   * @param eqt
   *          The equilibrator temperature.
   * @param sst
   *          The water temperature.
   * @return The pCO<sub>2</sub> at water temperature.
   */
  public static DoubleWithUncertainty calcCO2AtSST(
    DoubleWithUncertainty co2AtEquilibrator, DoubleWithUncertainty eqt,
    DoubleWithUncertainty sst) {

    return kelvin(sst).subtract(kelvin(eqt)).multiply(0.0423).exp()
      .multiply(co2AtEquilibrator);
  }
}
