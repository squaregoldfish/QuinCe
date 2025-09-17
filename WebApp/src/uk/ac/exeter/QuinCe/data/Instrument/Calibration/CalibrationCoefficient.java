package uk.ac.exeter.QuinCe.data.Instrument.Calibration;

import java.util.List;
import java.util.Objects;

import uk.ac.exeter.QuinCe.web.html.SelectItem;

/**
 * Simple object for a single calibration coefficient
 */
public class CalibrationCoefficient implements Cloneable {

  /**
   * Indicates a text input.
   *
   * @see #type
   */
  protected static final int TYPE_TEXT = 0;

  /**
   * Indicates a radio button set.
   *
   * @see #type
   */
  protected static final int TYPE_RADIO = 1;

  /**
   * The type of input that should be displayed for the user to enter a value.
   */
  private int type;

  /**
   * The coefficient's name
   */
  private String name;

  /**
   * The coefficient value
   */
  private String value = "0";

  /**
   * The items to be displayed in a radio button set, dropdown menu etc.
   */
  private List<SelectItem> selectItems = null;

  /**
   * Creates an empty (zero) coefficient with a default text input type.
   *
   * @param name
   *          The coefficient name.
   */
  protected CalibrationCoefficient(String name) {
    this.name = name;
    this.type = TYPE_TEXT;
  }

  /**
   * Create an empty (zero) coefficient with the specified input type.
   *
   * @param name
   *          The coefficient name.
   * @param type
   *          The input type.
   */
  protected CalibrationCoefficient(String name, int type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Copy constructor
   *
   * @param name
   * @param value
   * @param type
   */
  protected CalibrationCoefficient(CalibrationCoefficient source) {
    this.name = source.name;
    this.value = source.value;
    this.type = source.type;
    this.selectItems = source.selectItems;
  }

  /**
   * Get the coefficient name
   *
   * @return The coefficient's name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the coefficient value
   *
   * @return The value
   */
  public String getValue() {
    return value;
  }

  /**
   * Get the items to be displayed in a radio button set, dropdown menu etc.
   *
   * @return
   */
  public List<SelectItem> getSelectItems() {
    return selectItems;
  }

  /**
   * Set the items to be displayed in a radio button set, dropdown menu etc.
   */
  public void setSelectItems(List<SelectItem> selectItems) {
    this.selectItems = selectItems;
  }

  public Double getDoubleValue() {
    return Double.parseDouble(value);
  }

  /**
   * Set the coefficient value
   *
   * @param value
   *          The value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Get the input type to be used for getting a value.
   *
   * @return The input type.
   */
  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + ": " + value;
  }

  @Override
  public Object clone() {
    return new CalibrationCoefficient(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CalibrationCoefficient other = (CalibrationCoefficient) obj;
    return Objects.equals(name, other.name)
      && Objects.equals(value, other.value);
  }
}
