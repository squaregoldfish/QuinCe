package uk.ac.exeter.QuinCe.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

/**
 * Represents a {@link BigDecimal} value with an associated uncertainty.
 *
 * <p>
 * The uncertainty is optional; if there is no uncertainty, it will be set to
 * {@link Float#NaN}. Mathematical operations performed on values where at least
 * one of the uncertainties is {@link Float#NaN} will always result in the
 * output's uncertainty being {@link Float#NaN}.
 * </p>
 *
 * <p>
 * All calculations using {@link BigDecimalWithUncertainty} objects assume that
 * the uncertainties are independent.
 * </p>
 *
 * <p>
 * <b>REMEMBER:</b> Chaining operations on these objects will execute them in
 * left-to-right order because they are separate function calls. Normal
 * arithmetic order of operations will not apply.
 * </p>
 */
public record BigDecimalWithUncertainty(BigDecimal value, Float uncertainty)
  implements Comparable<BigDecimalWithUncertainty> {

  /**
   * Shortcut value for 1 with now uncertainty.
   */
  public static final BigDecimalWithUncertainty ONE = new BigDecimalWithUncertainty(
    BigDecimal.ONE, 0F);

  /**
   * Shortcut value for 0 with now uncertainty.
   */
  public static final BigDecimalWithUncertainty ZERO = new BigDecimalWithUncertainty(
    BigDecimal.ZERO, 0F);

  /**
   * Base constructor.
   *
   * <p>
   * A {@code null} {@code value} will result in a {@link NullPointerException}.
   * A {@code null} {@code uncertainty} will be converted to {@link Float#NaN}.
   * </p?
   *
   * @param value
   *          The value.
   * @param uncertainty
   *          The uncertainty.
   */
  public BigDecimalWithUncertainty(BigDecimal value, Float uncertainty) {
    if (null == value) {
      throw new NullPointerException();
    } else {
      this.value = value;
      this.uncertainty = null == uncertainty ? Float.NaN : uncertainty;
    }
  }

  /**
   * Create a value with no specified uncertainty.
   *
   * @param value
   *          The value.
   */
  public BigDecimalWithUncertainty(BigDecimal value) {
    this(value, Float.NaN);
  }

  /**
   * Create a value with no specified uncertainty.
   *
   * @param value
   *          The value.
   */
  public BigDecimalWithUncertainty(int value) {
    this(new BigDecimal(value), Float.NaN);
  }

  /**
   * Create a value from a {@link DoubleWithUncertainty}.
   *
   * @param value
   *          The value.
   * @see BigDecimal#BigDecimal(Double)
   */
  public BigDecimalWithUncertainty(DoubleWithUncertainty dwu) {
    this(new BigDecimal(dwu.value()), dwu.uncertainty());
  }

  /**
   * Create a value from a {@link #Double} with an uncertainty.
   *
   * @param value
   *          The value.
   * @param uncertainty
   *          The uncertainty.
   */
  public BigDecimalWithUncertainty(Double value, float uncertainty) {
    this(new BigDecimal(value), uncertainty);
  }

  /**
   * Determine whether or not this value has an uncertainty.
   *
   * @return {@code true} if the value has an uncertainty; {@code false} if it
   *         does not.
   */
  public boolean hasUncertainty() {
    return !uncertainty.isNaN();
  }

  /**
   * Get the value (without uncertainty) as a {@link String}.
   *
   * @return The String representation of the value.
   */
  public String stringValue() {
    return String.valueOf(value);
  }

  @Override
  public String toString() {
    if (!hasUncertainty()) {
      return stringValue();
    } else {
      return value.toString() + "±" + uncertainty.toString();
    }
  }

  /**
   * Calculate the equal-weight arithmetic mean of a {@link Collection} of
   * {@link BigDecimalWithUncertainty} objects.
   *
   * <p>
   * {@code NaN} or {@code null} objects are ignored. If any of the values have
   * a {@code NaN} uncertainty, the returned mean value will have {@code NaN}
   * uncertainty.
   * </p>
   *
   * @param values
   *          The values.
   * @return The mean.
   */
  public static BigDecimalWithUncertainty mean(
    Collection<BigDecimalWithUncertainty> values) {

    BigDecimalWithUncertainty result;

    // Filter out null/NaNs
    Collection<BigDecimalWithUncertainty> filtered = values.stream()
      .filter(v -> null != v).toList();

    if (filtered.size() == 0) {
      throw new ArithmeticException("Cannot calculate mean from no values");
    } else {
      BigDecimal total = new BigDecimal(0);

      for (BigDecimalWithUncertainty v : filtered) {
        total = total.add(v.value());
      }

      BigDecimal meanValue = total.divide(new BigDecimal(filtered.size()));

      Double uncertaintySqSum = 0D;

      for (BigDecimalWithUncertainty value : filtered) {
        if (!value.hasUncertainty()) {
          uncertaintySqSum = Double.NaN;
          break;
        } else {
          uncertaintySqSum += Math.pow(value.uncertainty(), 2);
        }
      }

      Float meanUncertainty = uncertaintySqSum.isNaN() ? Float.NaN
        : (float) Math.sqrt(uncertaintySqSum) / filtered.size();

      result = new BigDecimalWithUncertainty(meanValue, meanUncertainty);
    }

    return result;
  }

  /**
   * Subtract the specified value from this value.
   *
   * @param subtrahend
   *          The value to be subtracted from this value.
   * @return A new {@link BigDecimalWithUncertainty} containing the result of
   *         the subtraction.
   */
  public BigDecimalWithUncertainty subtract(
    BigDecimalWithUncertainty subtrahend) {
    return new BigDecimalWithUncertainty(value.subtract(subtrahend.value),
      (float) Math.sqrt(
        (Math.pow(uncertainty, 2) + Math.pow(subtrahend.uncertainty, 2))));
  }

  public BigDecimalWithUncertainty subtract(BigDecimal subtrahend) {
    return subtract(new BigDecimalWithUncertainty(subtrahend, 0F));
  }

  /**
   * Add the specified value to this value.
   *
   * @param addend
   *          The value to be added to this value.
   * @return A new {@link BigDecimalWithUncertainty} containing the result of
   *         the addition.
   */
  public BigDecimalWithUncertainty add(BigDecimalWithUncertainty addend) {
    return new BigDecimalWithUncertainty(value.add(addend.value()), (float) Math
      .sqrt(Math.pow(uncertainty, 2) + Math.pow(addend.uncertainty, 2)));
  }

  /**
   * Add the specified value to this value.
   *
   * @param addend
   *          The value to be added to this value.
   * @return A new {@link BigDecimalWithUncertainty} containing the result of
   *         the addition.
   */
  public BigDecimalWithUncertainty add(BigDecimal addend) {
    return add(new BigDecimalWithUncertainty(addend, 0F));
  }

  /**
   * Multiply this value with the specified value.
   *
   * @param multiplier
   *          The multiplier.
   * @return A new {@link BigDecimalWithUncertainty} containing the
   *         multiplication result.
   */
  public BigDecimalWithUncertainty multiply(
    BigDecimalWithUncertainty multiplier) {
    BigDecimal result = value.multiply(multiplier.value);

    Float uncertainty = (float) result.doubleValue()
      * relativeUncertainty(this, multiplier).floatValue();

    return new BigDecimalWithUncertainty(result, uncertainty);
  }

  /**
   * Multiply this value with the specified value.
   *
   * @param multiplier
   *          The multiplier.
   * @return A new {@link BigDecimalWithUncertainty} containing the
   *         multiplication result.
   */
  public BigDecimalWithUncertainty multiply(BigDecimal multiplier) {
    return multiply(new BigDecimalWithUncertainty(multiplier, 0F));
  }

  /**
   * Divide this value by the specified value.
   *
   * @param divisor
   *          The divisor.
   * @return A new {@link BigDecimalWithUncertainty} containing the division
   *         result.
   */
  public BigDecimalWithUncertainty divide(BigDecimalWithUncertainty divisor) {
    BigDecimal result = value.divide(divisor.value, MathContext.DECIMAL128);
    Float uncertainty = (float) result.doubleValue()
      * relativeUncertainty(this, divisor).floatValue();

    return new BigDecimalWithUncertainty(result, uncertainty);
  }

  public BigDecimalWithUncertainty divide(BigDecimal divisor) {
    return divide(new BigDecimalWithUncertainty(divisor, 0F));
  }

  /**
   * Calculate the relative uncertainty for multiplication/division operations.
   *
   * @param a
   *          The first value.
   * @param b
   *          The second value.
   * @return The relative uncertainty.
   */
  private static Float relativeUncertainty(BigDecimalWithUncertainty a,
    BigDecimalWithUncertainty b) {
    /*
     * Uncertainty = result * SQRT( (σA / A)² + (σB / B)² )
     *
     * The SQRT part is termed the relative uncertainty
     */

    if (a.uncertainty == 0F && b.uncertainty == 0F) {
      return 0F;
    } else if (a.uncertainty.isNaN() || b.uncertainty.isNaN()) {
      return Float.NaN;
    } else {
      BigDecimal aPart = new BigDecimal(a.uncertainty)
        .divide(a.value, MathContext.DECIMAL128).pow(2);
      BigDecimal bPart = new BigDecimal(b.uncertainty)
        .divide(b.value, MathContext.DECIMAL128).pow(2);

      BigDecimal partSum = aPart.add(bPart);
      return partSum.sqrt(MathContext.DECIMAL128).floatValue();
    }
  }

  /**
   * Calculate the natural log of this value.
   *
   * <p>
   * This method 'cheats' by using the double-precision {@link Math#log}
   * function, so will not maintain the precision of the original
   * {@link BigDecimal}.
   * </p>
   *
   * @return The natural log.
   */
  public BigDecimalWithUncertainty log() {
    if (value.equals(0D)) {
      throw new ArithmeticException();
    } else {
      BigDecimal result = new BigDecimal(Math.log(value.doubleValue()));
      BigDecimal resultUncertainty = new BigDecimal(uncertainty).divide(value,
        MathContext.DECIMAL128);
      return new BigDecimalWithUncertainty(result,
        resultUncertainty.floatValue());
    }
  }

  /**
   * Calculate {@code e<sup>this</sup>}.
   *
   * <p>
   * This method 'cheats' by using the double-precision {@link Math#exp}
   * function, so will not maintain the precision of the original
   * {@link BigDecimal}.
   * </p>
   *
   * @return {@code e<sup>this</sup>}.
   */
  public BigDecimalWithUncertainty exp() {
    Double result = Math.exp(value.doubleValue());
    return new BigDecimalWithUncertainty(new BigDecimal(result),
      (float) (result * uncertainty));
  }

  /**
   * Calculate the log<sub>10</sub> of this value.
   *
   * <p>
   * The uncertainty is calculated as {@code 0.4343(σ/x)}, derived from
   * {@code logₙ(x) ± σ/x∙ln(x)}
   * </p>
   *
   * @return The log<sub>10</sub> of this value.
   */
  public BigDecimalWithUncertainty log10() {
    if (value.equals(0D)) {
      throw new ArithmeticException();
    } else {
      Double result = Math.log10(value.doubleValue());
      double resultUncertainty = (uncertainty / value.doubleValue()) * 0.4343;
      return new BigDecimalWithUncertainty(result, (float) resultUncertainty);
    }
  }

  /**
   * Calculate {@code thisⁿ}.
   *
   * @param n
   *          The exponent.
   * @return {@code thisⁿ}.
   */
  public BigDecimalWithUncertainty pow(int n) {

    if (n == 0) {
      return new BigDecimalWithUncertainty(BigDecimal.ONE, 0F);
    } else {
      BigDecimal result = value.pow(n);
      BigDecimal resultUncertainty = new BigDecimal(Math.abs(n))
        .multiply(value.pow(n - 1)).multiply(new BigDecimal(uncertainty));

      return new BigDecimalWithUncertainty(result,
        resultUncertainty.floatValue());
    }
  }

  /**
   * Get this value as a {@link DoubleWithUncertainty}.
   *
   * @return The {@link DoubleWithUncertainty}.
   * @see BigDecimal#doubleValue()
   */
  public DoubleWithUncertainty toDoubleWithUncertainty() {
    return new DoubleWithUncertainty(value.doubleValue(), uncertainty);
  }

  @Override
  public int compareTo(BigDecimalWithUncertainty o) {
    return value.compareTo(o.value);
  }
}
