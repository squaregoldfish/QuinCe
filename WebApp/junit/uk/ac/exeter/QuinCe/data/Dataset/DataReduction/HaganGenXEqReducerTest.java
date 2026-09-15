package uk.ac.exeter.QuinCe.data.Dataset.DataReduction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

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
    double temp = 51.40766666666665D;
    double pressure = 100.98844999999999D;
    double raw1 = 4600480.75D;
    double raw2 = 4975966.1D;
    double rh = 7.33916666666667D;
    double rhTemp = 30.406000000000027D;
    double zeroCalK = 0.98369240100922251D;
    double spanCalKseed = 0.803046D;
    double spanRef = 457.4D;
    double spanSlope = -0.0761287;

    double converged = HaganGenXEqReducer.convergeSpanCalK(temp, pressure, raw1,
      raw2, rh, rhTemp, zeroCalK, spanCalKseed, spanRef, spanSlope);

    assertEquals(0.80210921D, converged, 0.0000001D);
  }

  @Test
  public void calculatedCO2Test() {
    double temp = 51.40766666666666D;
    double pressure = 101.005699999999998D;
    double raw1 = 4716590.4D;
    double raw2 = 4975902.55D;
    double rh = 7.387833333333335D;
    double rhTemp = 30.521166666666666D;
    double zeroCalK = 0.983724D;
    double spanCalK = 0.803046D;
    MutableDouble r_absp = new MutableDouble(0D);
    MutableDouble s_absp = new MutableDouble(0D);
    double spanSlope = -0.0761287;

    double xCO2Wet = HaganGenXEqReducer.calculatedCO2(temp, pressure, raw1,
      raw2, rh, rhTemp, zeroCalK, spanCalK, r_absp, s_absp, spanSlope);

    assertEquals(309.15475554272649D, xCO2Wet, 0.0000001D);
  }
}
