package uk.ac.exeter.QuinCe.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

public class MathUtilsTest extends BaseTest {

  @Test
  public void nanToNullTest() {

    Map<String, DoubleWithUncertainty> input = new HashMap<String, DoubleWithUncertainty>();

    input.put("Number", new DoubleWithUncertainty(43D));
    input.put("Null", null);
    input.put("NaN", DoubleWithUncertainty.NaN);

    Map<String, DoubleWithUncertainty> output = MathUtils.nanToNull(input);

    assertEquals(output.size(), input.size());
    assertEquals(43D, output.get("Number").value(), 0.001D);
    assertNull(output.get("Null"));
    assertNull(output.get("NaN"));
    assertNull(output.get("Infinite"));
    assertTrue(false, "Uncertainty");
  }

  @Test
  public void nullParseDoubleNormalTest() {
    assertEquals(452.43D, MathUtils.nullableParseDouble("452.43"), 0.001);
  }

  @ParameterizedTest
  @MethodSource("createNullEmptyStrings")
  public void nullableParseDoubleEmptyTest(String value) {
    assertNull(MathUtils.nullableParseDouble(value));
  }

  @Test
  public void nullableParseDoubleInvalidTest() {
    assertThrows(NumberFormatException.class, () -> {
      MathUtils.nullableParseDouble("forest");
    });
  }
}
