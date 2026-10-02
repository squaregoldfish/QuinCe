package uk.ac.exeter.QuinCe.utils;

import java.math.BigDecimal;

import org.apache.commons.math3.util.Precision;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertion to test the value and uncertainty of a
 * {@link BigDecimalWithUncertainty}.
 */
public class BigDecimalWithUncertaintyAssert extends
  AbstractAssert<BigDecimalWithUncertaintyAssert, BigDecimalWithUncertainty> {

  private final String name;

  public BigDecimalWithUncertaintyAssert(BigDecimalWithUncertainty actual) {
    super(actual, BigDecimalWithUncertaintyAssert.class);
    this.name = null;
  }

  public BigDecimalWithUncertaintyAssert(BigDecimalWithUncertainty actual,
    String name) {
    super(actual, BigDecimalWithUncertaintyAssert.class);
    this.name = name;
  }

  public static BigDecimalWithUncertaintyAssert assertThat(
    BigDecimalWithUncertainty actual) {
    return new BigDecimalWithUncertaintyAssert(actual);
  }

  public static BigDecimalWithUncertaintyAssert assertThat(
    BigDecimalWithUncertainty actual, String name) {
    return new BigDecimalWithUncertaintyAssert(actual, name);
  }

  /**
   * Test that a {@link BigDecimalWithUncertainty} has the specified value and
   * uncertainty.
   *
   * @param value
   *          The expected value.
   * @param uncertainty
   *          The expected uncertainty.
   * @return The assertion.
   */
  public BigDecimalWithUncertaintyAssert matches(BigDecimal value,
    Float uncertainty) {

    if (!Precision.equals(value.doubleValue(), actual.value().doubleValue(),
      0.0001D)) {
      failWithMessage("Value is incorrect: expected <%s>, was <%s>", value,
        actual.value());
    } else if (uncertainty.isNaN()) {
      if (!actual.uncertainty().isNaN()) {
        failWithMessage("Uncertainty is incorrect: expected <NaN>, was <%s>",
          actual.uncertainty());
      }
    } else if (!Precision.equals(uncertainty, actual.uncertainty(), 0.0001D)) {
      failWithMessage("Uncertainty is incorrect: expected <%s>, was <%s>",
        uncertainty, actual.uncertainty());
    }

    return this;
  }

  @Override
  protected void failWithMessage(String errorMessage, Object... arguments) {
    if (null != name) {
      throw failure(name + ": " + errorMessage, arguments);
    } else {
      throw failure(errorMessage, arguments);
    }
  }
}
