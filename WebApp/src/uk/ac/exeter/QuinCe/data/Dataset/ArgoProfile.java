package uk.ac.exeter.QuinCe.data.Dataset;

import com.google.gson.Gson;

/**
 * Represents an Argo cycle.
 *
 * <p>
 * A Cycle is a subset of an {@link ArgoCoordinate}, containing just the cycle
 * number, direction and profile.
 * </p>
 *
 * <p>
 * This class exists because a profile is the granularity at which users will
 * work with Argo data.
 * </p>
 */
public record ArgoProfile(int cycleNumber, char direction, int profile) {
  private static Gson gson = new Gson();

  /**
   * Create a profile from an {@link ArgoCoordinate}.
   *
   * @param coordinate
   *          The source coordinate.
   */
  public ArgoProfile(ArgoCoordinate coordinate) {
    this(coordinate.getCycleNumber(), coordinate.getDirection(),
      coordinate.getNProf());
  }

  public String toJson() {
    return gson.toJson(this);
  }
}
