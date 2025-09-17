package uk.ac.exeter.QuinCe.web.html;

/**
 * A very basic {@code record} class for items in PrimeFaces SelectOneMenu,
 * SelectOneRadio etc.
 *
 * <p>
 * {@code get} methods are supplied because PrimeFaces doesn't know how to
 * access record classes properly.
 * </p>
 */
public record SelectItem(String label, String value) {

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }
}
