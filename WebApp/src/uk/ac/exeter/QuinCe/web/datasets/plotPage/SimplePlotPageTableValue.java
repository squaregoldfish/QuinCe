package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import java.time.LocalDateTime;

import uk.ac.exeter.QuinCe.data.Dataset.QC.Flag;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.StringUtils;

public class SimplePlotPageTableValue implements PlotPageTableValue {

  /**
   * The displayed value.
   */
  private final String value;

  /**
   * The value's QC flag.
   */
  private final Flag qcFlag;

  /**
   * The QC message.
   */
  private final String qcMessage;

  /**
   * Indicates whether or not user QC is required.
   */
  private final boolean flagNeeded;

  private final char type;

  /**
   * Simple constructor with all values.
   *
   * @param value
   *          The value.
   * @param used
   *          Whether the value is used in a calculation.
   * @param qcFlag
   *          The QC flag.
   * @param qcMessage
   *          The QC message.
   * @param flagNeeded
   *          Whether or not user QC is required.
   */
  public SimplePlotPageTableValue(String value, Flag qcFlag, String qcMessage,
    boolean flagNeeded, char type) {
    this.value = StringUtils.formatNumber(value);
    this.qcFlag = qcFlag;
    this.qcMessage = qcMessage;
    this.flagNeeded = flagNeeded;
    this.type = type;
  }

  /**
   * Constructor for a timestamp.
   *
   * @param time
   *          The timestamp.
   */
  public SimplePlotPageTableValue(LocalDateTime time, boolean milliseconds) {

    if (milliseconds) {
      this.value = String.valueOf(DateTimeUtils.dateToLong(time));
    } else {
      this.value = DateTimeUtils.toIsoDate(time);
    }

    this.qcFlag = Flag.GOOD;
    this.qcMessage = "";
    this.flagNeeded = false;
    this.type = PlotPageTableValue.MEASURED_TYPE;
  }

  /**
   * Get the value string.
   *
   * @return The value.
   */
  @Override
  public String getValue() {
    return value;
  }

  /**
   * Get the QC flag.
   *
   * @return The QC flag.
   */
  @Override
  public Flag getQcFlag() {
    return qcFlag;
  }

  /**
   * Get the QC message.
   *
   * @return The QC message.
   */
  @Override
  public String getQcMessage() {
    return qcMessage;
  }

  @Override
  public boolean getFlagNeeded() {
    return flagNeeded;
  }

  @Override
  public long getId() {
    return -1L;
  }

  @Override
  public boolean isNull() {
    return null == value;
  }

  @Override
  public char getType() {
    return type;
  }

  @Override
  public String toString() {
    return value;
  }
}
