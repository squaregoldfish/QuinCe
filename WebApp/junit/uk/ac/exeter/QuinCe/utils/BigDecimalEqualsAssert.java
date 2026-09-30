package uk.ac.exeter.QuinCe.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.assertj.core.api.AbstractAssert;

public class BigDecimalEqualsAssert
  extends AbstractAssert<BigDecimalEqualsAssert, BigDecimal> {

  public BigDecimalEqualsAssert(BigDecimal actual) {
    super(actual, BigDecimalEqualsAssert.class);
  }

  public static BigDecimalEqualsAssert assertThat(BigDecimal actual) {
    return new BigDecimalEqualsAssert(actual);
  }

  public BigDecimalEqualsAssert matches(BigDecimal value, int precision) {

    MathContext context = new MathContext(precision, RoundingMode.HALF_UP);

    BigDecimal actualRounded = actual.round(context);
    BigDecimal valueRounded = value.round(context);

    if (actualRounded.compareTo(valueRounded) != 0) {
      failWithMessage("Value is incorrect: expected <%s>, was <%s>",
        valueRounded, actualRounded);
    }

    return this;
  }
}
