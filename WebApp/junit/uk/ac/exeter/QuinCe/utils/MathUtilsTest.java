package uk.ac.exeter.QuinCe.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.ac.exeter.QuinCe.TestBase.BaseTest;

public class MathUtilsTest extends BaseTest {

  @Test
  public void nanToNullTest() {

    Map<String, Double> input = new HashMap<String, Double>();

    input.put("Number", 43D);
    input.put("Null", null);
    input.put("NaN", Double.NaN);
    input.put("Infinite", Double.POSITIVE_INFINITY);

    Map<String, Double> output = MathUtils.nanToNull(input);

    assertEquals(output.size(), input.size());
    assertEquals(43D, output.get("Number"), 0.001D);
    assertNull(output.get("Null"));
    assertNull(output.get("NaN"));
    assertNull(output.get("Infinite"));
  }
}
