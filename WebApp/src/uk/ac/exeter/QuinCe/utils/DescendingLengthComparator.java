package uk.ac.exeter.QuinCe.utils;

import java.util.Comparator;

/**
 * Compares {@link String}s by descending order of length.
 *
 * <p>
 * {@code null} strings are considered to be shorter than zero-length strings.
 * Strings of equal length are not sorted further.
 * </p>
 */
public class DescendingLengthComparator implements Comparator<String> {

  @Override
  public int compare(String o1, String o2) {
    int result;

    if (null == o1 && null == o2) {
      result = 0;
    } else if (null == o1) {
      result = 1;
    } else if (null == o2) {
      result = -1;
    } else {
      result = o2.length() - o1.length();
    }

    return result;
  }
}
