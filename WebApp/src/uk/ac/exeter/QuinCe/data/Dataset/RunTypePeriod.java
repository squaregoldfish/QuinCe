package uk.ac.exeter.QuinCe.data.Dataset;

public class RunTypePeriod {

  private String runType;

  private Coordinate start;

  private Coordinate end;

  protected RunTypePeriod(String runType, Coordinate start) {
    this.runType = runType;
    this.start = start;
    this.end = start;
  }

  public String getRunType() {
    return runType;
  }

  public Coordinate getStart() {
    return start;
  }

  public Coordinate getEnd() {
    return end;
  }

  protected void setEnd(Coordinate end) {
    this.end = end;
  }

  public boolean encompasses(Coordinate coordinate) {
    boolean result = false;
    if (start.equals(end)) {
      result = coordinate.equals(start);
    } else {
      boolean afterStart = false;
      boolean beforeEnd = false;

      if (coordinate.equals(start) || coordinate.isAfter(start)) {
        afterStart = true;
      }

      if (coordinate.equals(end) || coordinate.isBefore(end)) {
        beforeEnd = true;
      }

      result = afterStart && beforeEnd;
    }

    return result;
  }

}
