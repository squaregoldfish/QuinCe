package uk.ac.exeter.QuinCe.utils;

import org.apache.commons.math3.util.Precision;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertion to test the value and uncertainty of a
 * {@link DoubleWithUncertainty}.
 */
public class DoubleWithUncertaintyAssert
  extends AbstractAssert<DoubleWithUncertaintyAssert, DoubleWithUncertainty> {

  private final String name;

  public DoubleWithUncertaintyAssert(DoubleWithUncertainty actual) {
    super(actual, DoubleWithUncertaintyAssert.class);
    this.name = null;
  }

  public DoubleWithUncertaintyAssert(DoubleWithUncertainty actual,
    String name) {
    super(actual, DoubleWithUncertaintyAssert.class);
    this.name = name;
  }

  public static DoubleWithUncertaintyAssert assertThat(
    DoubleWithUncertainty actual) {
    return new DoubleWithUncertaintyAssert(actual);
  }

  public static DoubleWithUncertaintyAssert assertThat(
    DoubleWithUncertainty actual, String name) {
    return new DoubleWithUncertaintyAssert(actual, name);
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

  /**
   * Test that a {@link DoubleWithUncertainty} matches the supplied object.
   *
   * @param value
   *          The test value.
   * @return The assertion.
   */
  public DoubleWithUncertaintyAssert matches(DoubleWithUncertainty test) {

    if (test.value().isNaN()) {
      if (!actual.value().isNaN()) {
        failWithMessage("Value is incorrect: expected <NaN>, was <%s>",
          actual.value());
      }
    } else if (!Precision.equals(test.value(), actual.value(), 0.0001D)) {
      failWithMessage("Value is incorrect: expected <%s>, was <%s>",
        test.value(), actual.value());
    } else if (test.uncertainty().isNaN()) {
      if (!actual.uncertainty().isNaN()) {
        failWithMessage("Uncertainty is incorrect: expected <NaN>, was <%s>",
          actual.uncertainty());
      }
    } else if (!Precision.equals(test.uncertainty(), actual.uncertainty(),
      0.0001D)) {
      failWithMessage("Uncertainty is incorrect: expected <%s>, was <%s>",
        test.uncertainty(), actual.uncertainty());
    }

    return this;
  }

  public DoubleWithUncertaintyAssert nan() {
    if (!actual.value().isNaN()) {
      failWithMessage("Value is not NaN");
    }
    if (!actual.uncertainty().isNaN()) {
      failWithMessage("Uncertainty is not NaN");
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
