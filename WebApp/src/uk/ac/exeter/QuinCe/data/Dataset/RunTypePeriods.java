package uk.ac.exeter.QuinCe.data.Dataset;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class RunTypePeriods extends ArrayList<RunTypePeriod> {

  boolean finished = false;

  public RunTypePeriods() {
    super();
  }

  public void add(String runType, Coordinate coordinate)
    throws DataSetException {

    if (finished) {
      throw new DataSetException("RunTypePeriods is finished");
    }

    if (size() == 0) {
      add(new RunTypePeriod(runType, coordinate));
    } else {
      if (coordinate.isBefore(getLastCoordinate())
        || coordinate.equals(getLastCoordinate())) {
        throw new DataSetException(
          "Added time must be after last period end time");
      }

      RunTypePeriod currentPeriod = get(size() - 1);
      if (!currentPeriod.getRunType().equals(runType)) {
        add(new RunTypePeriod(runType, coordinate));
      } else {
        currentPeriod.setEnd(coordinate);
      }
    }
  }

  /**
   * Signal that the last run time has been registered
   */
  public void finish() {
    if (size() > 0) {
      get(size() - 1).setEnd(Coordinate.MAX);
    }

    finished = true;
  }

  public boolean contains(Coordinate coordinate) {
    boolean result = false;

    for (RunTypePeriod period : this) {
      if (period.encompasses(coordinate)) {
        result = true;
        break;
      }
    }

    return result;
  }

  public String getRunType(Coordinate coordinate) {
    Optional<RunTypePeriod> period = stream()
      .filter(p -> p.encompasses(coordinate)).findAny();

    return period.isEmpty() ? null : period.get().getRunType();
  }

  /**
   * Get the unique set of run type names from all periods. The order is not
   * guaranteed.
   *
   * @return The run type names.
   */
  public Set<String> getRunTypeNames() {
    return stream().map(p -> p.getRunType()).distinct()
      .collect(Collectors.toSet());
  }

  private Coordinate getLastCoordinate() {
    Coordinate result = null;

    if (size() > 0) {
      result = get(size() - 1).getEnd();
    }

    return result;
  }
}
