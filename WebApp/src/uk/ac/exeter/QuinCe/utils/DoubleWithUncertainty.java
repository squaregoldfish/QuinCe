package uk.ac.exeter.QuinCe.utils;

import java.util.Collection;

/**
 * Represents a value with an associated uncertainty.
 *
 * <p>
 * A {@link Double.NaN} value is allowed, in which case the uncertainty will
 * also be forced to be {@link Float.NaN}. Any arithmetical operation on a
 * {@link Double.NaN} value will always give a {@link Double.NaN} result.
 * </p>
 *
 * <p>
 * The uncertainty is optional; if there is no uncertainty, it will be set to
 * {@link Float#NaN}. Mathematical operations performed on values where at least
 * one of the uncertainties is {@link Float#NaN} will always result in the
 * output's uncertainty being {@link Float#NaN}.
 * </p>
 *
 * <p>
 * All calculations using {@link DoubleWithUncertainty} objects assume that the
 * uncertainties are independent.
 * </p>
 *
 * <p>
 * <b>REMEMBER:</b> Chaining operations on these objects will execute them in
 * left-to-right order because they are separate function calls. Normal
 * arithmetic order of operations will not apply.
 * </p>
 */
public record DoubleWithUncertainty(Double value, Float uncertainty)
  implements Comparable<DoubleWithUncertainty> {

  /**
   * Fixed representation of a {@code NaN} value.
   */
  public static final DoubleWithUncertainty NaN = new DoubleWithUncertainty(
    Double.NaN);

  /**
   * Shortcut value for 1 with no uncertainty.
   */
  public static final DoubleWithUncertainty ZERO = new DoubleWithUncertainty(0D,
    0F);

  /**
   * Shortcut value for 1 with no uncertainty.
   */
  public static final DoubleWithUncertainty ONE = new DoubleWithUncertainty(1D,
    0F);

  /**
   * Shortcut value for 1 with no uncertainty.
   */
  public static final DoubleWithUncertainty HUNDRED = new DoubleWithUncertainty(
    100D, 0F);

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
  public DoubleWithUncertainty(Double value, Float uncertainty) {
    if (null == value) {
      throw new NullPointerException();
    } else if (value.isNaN()) {
      this.value = value;
      this.uncertainty = Float.NaN;
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
  public DoubleWithUncertainty(Double value) {
    this(value, Float.NaN);
  }

  /**
   * Create a value from a simple {@link Long} with no uncertainty.
   *
   * @param value
   *          The value.
   */
  public DoubleWithUncertainty(Long value) {
    this(value.doubleValue(), Float.NaN);
  }

  /**
   * Create a value from a simple {@link Integer} with no uncertainty.
   *
   * @param value
   *          The value.
   */
  public DoubleWithUncertainty(Integer value) {
    this(value.doubleValue(), Float.NaN);
  }

  /**
   * Constructor for {@code int} values.
   *
   * @param value
   *          The value.
   * @param uncertainty
   *          The uncertainty.
   */
  public DoubleWithUncertainty(int value, int uncertainty) {
    this(Double.valueOf(value), Float.valueOf(uncertainty));
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
   * Determine whether or not this value is {@link Double#NaN}.
   *
   * @return {@code true} if the value is {@link Double#NaN}; {@code false} if
   *         it is not.
   */
  public boolean isNaN() {
    return value.isNaN();
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
    if (isNaN()) {
      return "NaN";
    } else if (!hasUncertainty()) {
      return stringValue();
    } else {
      return value.toString() + "±" + uncertainty.toString();
    }
  }

  /**
   * Calculate the equal-weight arithmetic mean of a {@link Collection} of
   * {@link DoubleWithUncertainty} objects.
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
  public static DoubleWithUncertainty mean(
    Collection<DoubleWithUncertainty> values) {

    DoubleWithUncertainty result;

    // Filter out null/NaNs
    Collection<DoubleWithUncertainty> filtered = values.stream()
      .filter(v -> null != v && !v.isNaN()).toList();

    if (filtered.size() == 0) {
      result = new DoubleWithUncertainty(Double.NaN);
    } else {

      Double meanValue = filtered.stream().mapToDouble(v -> v.value()).sum()
        / filtered.size();

      Double uncertaintySqSum = 0D;

      for (DoubleWithUncertainty value : filtered) {
        if (!value.hasUncertainty()) {
          uncertaintySqSum = Double.NaN;
          break;
        } else {
          uncertaintySqSum += Math.pow(value.uncertainty(), 2);
        }
      }

      Float meanUncertainty = uncertaintySqSum.isNaN() ? Float.NaN
        : (float) Math.sqrt(uncertaintySqSum) / filtered.size();

      result = new DoubleWithUncertainty(meanValue, meanUncertainty);
    }

    return result;
  }

  /**
   * Subtract the specified value from this value.
   *
   * @param subtrahend
   *          The value to be subtracted from this value.
   * @return A new {@link DoubleWithUncertainty} containing the result of the
   *         subtraction.
   */
  public DoubleWithUncertainty subtract(DoubleWithUncertainty subtrahend) {
    return new DoubleWithUncertainty(value - subtrahend.value(), (float) Math
      .sqrt((Math.pow(uncertainty, 2) + Math.pow(subtrahend.uncertainty, 2))));
  }

  /**
   * Subtract a constant (with assumed zero uncertainty) from this value.
   *
   * @param subtrahend
   *          The value to be subtracted.
   * @return The result of the subtraction.
   */
  public DoubleWithUncertainty subtract(double subtrahend) {
    return new DoubleWithUncertainty(value - subtrahend, uncertainty);
  }

  /**
   * Add the specified value to this value.
   *
   * @param augend
   *          The value to be added to this value.
   * @return A new {@link DoubleWithUncertainty} containing the result of the
   *         addition.
   */
  public DoubleWithUncertainty add(DoubleWithUncertainty augend) {
    return new DoubleWithUncertainty(value + augend.value(), (float) Math
      .sqrt(Math.pow(uncertainty, 2) + Math.pow(augend.uncertainty, 2)));
  }

  /**
   * Add a constant (with assumed zero uncertainty) to this value.
   *
   * @param augend
   *          The value to be add.
   * @return The result of the addition.
   */
  public DoubleWithUncertainty add(double augend) {
    return new DoubleWithUncertainty(value + augend, uncertainty);
  }

  /**
   * Multiply this value with the specified value.
   *
   * @param multiplier
   *          The multiplier.
   * @return A new {@link DoubleWithUncertainty} containing the multiplication
   *         result.
   */
  public DoubleWithUncertainty multiply(DoubleWithUncertainty multiplier) {
    Double result = value * multiplier.value;
    double uncertainty = result * relativeUncertainty(this, multiplier);
    return new DoubleWithUncertainty(result, (float) uncertainty);
  }

  /**
   * Multiply this value with the specified constant (with assumed zero
   * uncertainty).
   *
   * @param multiplier
   *          The multiplier.
   * @return A new {@link DoubleWithUncertainty} containing the multiplication
   *         result.
   */
  public DoubleWithUncertainty multiply(double multiplier) {
    return multiply(new DoubleWithUncertainty(multiplier, 0F));
  }

  /**
   * Divide this value by the specified value.
   *
   * @param divisor
   *          The divisor.
   * @return A new {@link DoubleWithUncertainty} containing the division result.
   */
  public DoubleWithUncertainty divide(DoubleWithUncertainty divisor) {
    Double result = value / divisor.value;
    double uncertainty = result * relativeUncertainty(this, divisor);
    return new DoubleWithUncertainty(result, (float) uncertainty);
  }

  /**
   * Divide this value by the specified constant (with assumed zero
   * uncertainty).
   *
   * @param divisor
   *          The divisor.
   * @return A new {@link DoubleWithUncertainty} containing the division result.
   */
  public DoubleWithUncertainty divide(double divisor) {
    return divide(new DoubleWithUncertainty(divisor, 0F));
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
  private static Double relativeUncertainty(DoubleWithUncertainty a,
    DoubleWithUncertainty b) {
    /*
     * Uncertainty = result * SQRT( (σA / A)² + (σB / B)² )
     *
     * The SQRT part is termed the relative uncertainty
     */

    if (a.uncertainty == 0F && b.uncertainty == 0F) {
      return 0D;
    } else {
      Double aPart = Math.pow(a.uncertainty / a.value, 2);
      Double bPart = Math.pow(b.uncertainty / b.value, 2);
      return Math.sqrt(aPart + bPart);
    }
  }

  /**
   * Determine whether or not a {@link DoubleWithUncertainty} has a value.
   *
   * <p>
   * If the passed value is {@code null} or {@link #isNaN()} is {@code true},
   * the object is determined not to have a value.
   * </p>
   *
   * @param value
   *          The value to be checked.
   * @return {@code true} if the value is {@code null} or does not contain a
   *         value; {@code false} if it does contain a value.
   */
  public static boolean isNaN(DoubleWithUncertainty value) {
    return value.isNaN();
  }

  /**
   * Calculate the natural log of this value.
   *
   * <p>
   * If the passed in value is {@code 0}, the result will be {@code NaN}.
   *
   * @return The natural log.
   */
  public DoubleWithUncertainty log() {
    if (value.equals(0D)) {
      return new DoubleWithUncertainty(Double.NaN);
    } else {
      return new DoubleWithUncertainty(Math.log(value),
        (float) (uncertainty / value));
    }
  }

  /**
   * Calculate {@code thisⁿ}.
   *
   * @param n
   *          The exponent.
   * @return {@code thisⁿ}.
   */
  public DoubleWithUncertainty pow(double n) {
    Double result = Math.pow(value, n);
    Double resultUncertainty = Math.abs(n) * Math.pow(value, n - 1)
      * uncertainty;

    return new DoubleWithUncertainty(result, resultUncertainty.floatValue());
  }

  /**
   * Calculate {@code e<sup>this</sup>}.
   *
   * @return {@code e<sup>this</sup>}.
   */
  public DoubleWithUncertainty exp() {
    Double result = Math.exp(value);
    return new DoubleWithUncertainty(result, (float) (result * uncertainty));
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
  public DoubleWithUncertainty log10() {
    if (value.equals(0D)) {
      return new DoubleWithUncertainty(Double.NaN);
    } else {
      Double result = Math.log10(value);
      double resultUncertainty = (uncertainty / value) * 0.4343;
      return new DoubleWithUncertainty(result, (float) resultUncertainty);
    }
  }

  @Override
  public int compareTo(DoubleWithUncertainty o) {
    return value.compareTo(o.value);
  }
}
