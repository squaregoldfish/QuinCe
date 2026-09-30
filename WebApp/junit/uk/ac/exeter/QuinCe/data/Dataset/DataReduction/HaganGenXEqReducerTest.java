package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;
import uk.ac.exeter.QuinCe.utils.BigDecimalEqualsAssert;
import uk.ac.exeter.QuinCe.utils.MutableBigDecimal;

/**
 * Tests for the Hagan GenX sensor.
 *
 * <p>
 * Individual method tests are based on live debugging of the GenX code from the
 * manufacturer.
 * </p>
 */
public class HaganGenXEqReducerTest extends BaseTest {

  @Test
  public void convergeSpanCalKTest() {
    BigDecimal temp = new BigDecimal(51.40766666666665D);
    BigDecimal pressure = new BigDecimal(100.98844999999999D);
    BigDecimal raw1 = new BigDecimal(4600480.75D);
    BigDecimal raw2 = new BigDecimal(4975966.1D);
    BigDecimal rh = new BigDecimal(7.33916666666667D);
    BigDecimal rhTemp = new BigDecimal(30.406000000000027D);
    BigDecimal zeroCalK = new BigDecimal(0.98369240100922251D);
    BigDecimal spanCalKseed = new BigDecimal(0.803046D);
    BigDecimal spanRef = new BigDecimal(457.4D);
    BigDecimal spanSlope = new BigDecimal(-0.0761287);

    BigDecimal converged = HaganGenXEqReducer.convergeSpanCalK(temp, pressure,
      raw1, raw2, rh, rhTemp, zeroCalK, spanCalKseed, spanRef, spanSlope);

    BigDecimalEqualsAssert.assertThat(converged)
      .matches(new BigDecimal(0.802109D), 6);
  }

  @Test
  public void calculatedCO2Test() {
    BigDecimal temp = new BigDecimal(51.40766666666666D);
    BigDecimal pressure = new BigDecimal(101.005699999999998D);
    BigDecimal raw1 = new BigDecimal(4716590.4D);
    BigDecimal raw2 = new BigDecimal(4975902.55D);
    BigDecimal rh = new BigDecimal(7.387833333333335D);
    BigDecimal rhTemp = new BigDecimal(30.521166666666666D);
    BigDecimal zeroCalK = new BigDecimal(0.983724D);
    BigDecimal spanCalK = new BigDecimal(0.803046D);
    MutableBigDecimal r_absp = new MutableBigDecimal(0D);
    MutableBigDecimal s_absp = new MutableBigDecimal(0D);
    BigDecimal spanSlope = new BigDecimal(-0.0761287D);

    BigDecimal xCO2Wet = HaganGenXEqReducer.calculatedCO2(temp, pressure, raw1,
      raw2, rh, rhTemp, zeroCalK, spanCalK, r_absp, s_absp, spanSlope);

    BigDecimalEqualsAssert.assertThat(xCO2Wet)
      .matches(new BigDecimal(309.154755D), 6);
  }
}
