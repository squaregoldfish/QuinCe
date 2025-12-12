package uk.ac.exeter.QuinCe.data.Dataset;

import com.google.gson.Gson;

/**
 * Represents an Argo profile.
 * 
 * <p>
 * A Profile is a subset of an {@link ArgoCoordinate}, containing just the cycle
 * number and direction.
 * </p>
 * 
 * <p>
 * This class exists because a profile is the granularity at which users will
 * work with Argo data.
 * </p>
 */
public record ArgoProfile(int cycleNumber, char direction) {
  private static Gson gson = new Gson();

  /**
   * Create a profile from an {@link ArgoCoordinate}.
   * 
   * @param coordinate
   *          The source coordinate.
   */
  public ArgoProfile(ArgoCoordinate coordinate) {
    this(coordinate.getCycleNumber(), coordinate.getDirection());
  }

  public String toJson() {
    return gson.toJson(this);
  }
}
