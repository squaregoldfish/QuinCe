package uk.ac.exeter.QuinCe.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

public class DoubleWithUncertaintyTest extends BaseTest {

  @Test
  public void goodConstructorTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, 2F);
    assertEquals(24D, value.value());
    assertEquals(2F, value.uncertainty());
  }

  @Test
  public void nullValueTest() {
    assertThrows(NullPointerException.class, () -> {
      new DoubleWithUncertainty(null, 2F);
    });
  }

  @Test
  public void nanValueTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(Double.NaN, 2F);
    assertEquals(Double.NaN, value.value());
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void nanUncertaintyTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, Float.NaN);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @Test
  public void nullUncertaintyTest() {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, null);
    assertEquals(Float.NaN, value.uncertainty());
  }

  @ParameterizedTest
  @CsvSource({ "1F, true", "NaN, false" })
  public void hasUncertaintyTest(Float uncertainty, boolean hasUncertainty) {
    DoubleWithUncertainty value = new DoubleWithUncertainty(24D, uncertainty);
    assertEquals(hasUncertainty, value.hasUncertainty());
  }

}
