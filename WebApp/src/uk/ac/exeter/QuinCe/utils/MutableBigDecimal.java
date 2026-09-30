package uk.ac.exeter.QuinCe.utils;

import java.math.BigDecimal;

/**
 * This is a mutable version of {@link BigDecimal}. It is a simple wrapper
 * around a {@link BigDecimal} object.
 *
 * <p>
 * The value can be set directly, or the standard {@link BigDecimal} methods can
 * be called to perform calculations and store the result in place. <i>Not all
 * methods are implemented at this time - they will be added as needed because I
 * am lazy.</i>
 * </p>
 *
 * <p>
 * The {@code null} value cannot be stored. Any attempt to do so will result in
 * a {@link NullPointerException}.
 * </p>
 */
public class MutableBigDecimal {

	private BigDecimal value;

	public MutableBigDecimal(BigDecimal value) {
		setValue(value);
	}

	public MutableBigDecimal(Double value) {
		setValue(new BigDecimal(value));
	}

	public void setValue(BigDecimal value) {
		if (null == value) {
			throw new NullPointerException();
		}

		this.value = value;
	}

	public BigDecimal getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
