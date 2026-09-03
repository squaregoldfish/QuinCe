package uk.ac.exeter.QuinCe.utils;

import org.apache.commons.math3.util.Precision;
import org.assertj.core.api.AbstractAssert;

public class DoubleWithUncertaintyAssert
  extends AbstractAssert<DoubleWithUncertaintyAssert, DoubleWithUncertainty> {

  public DoubleWithUncertaintyAssert(DoubleWithUncertainty actual) {
    super(actual, DoubleWithUncertaintyAssert.class);
  }

  public static DoubleWithUncertaintyAssert assertThat(
    DoubleWithUncertainty actual) {
    return new DoubleWithUncertaintyAssert(actual);
  }

  /**
   * Test that a {@link DoubleWithUncertainty} has the specified value and
   * uncertainty.
   * 
   * @param value
   *          The expected value.
   * @param uncertainty
   *          The expected uncertainty.
   * @return The assertion.
   */
  public DoubleWithUncertaintyAssert matches(Double value, Float uncertainty) {

    if (value.isNaN()) {
      if (!actual.value().isNaN()) {
        failWithMessage("Value is incorrect: expected <NaN>, was <%s>",
          actual.value());
      }
    } else if (!Precision.equals(value, actual.value(), 0.0001D)) {
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
}
