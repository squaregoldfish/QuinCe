package uk.ac.exeter.QuinCe.utils;

import java.util.ArrayList;
import java.util.List;

public class WeightedMeanCalculator {

  private List<DoubleWithUncertainty> values;

  private List<Double> weights;

  public WeightedMeanCalculator() {
    values = new ArrayList<DoubleWithUncertainty>();
    weights = new ArrayList<Double>();
  }

  public void add(DoubleWithUncertainty value, Double weight) {
    values.add(value);
    weights.add(weight);
  }

  public DoubleWithUncertainty getWeightedMean() {

    double weightedSum = 0D;
    double sumOfWeights = 0D;
    double weightedUncertaintySum = 0D;

    for (int i = 0; i < values.size(); i++) {

      if (!DoubleWithUncertainty.isNaN(values.get(i))) {
        weightedSum += values.get(i).value() * weights.get(i);
        sumOfWeights += weights.get(i);
        weightedUncertaintySum = Math.pow(weights.get(i), 2)
          * Math.pow(values.get(i).uncertainty(), 2);
      }
    }

    if (sumOfWeights == 0) {
      throw new IllegalArgumentException("Sum of weights is zero");
    }

    double mean = weightedSum / sumOfWeights;
    float uncertainty = (float) (Math.sqrt(weightedUncertaintySum)
      / sumOfWeights);

    return new DoubleWithUncertainty(mean, uncertainty);
  }

  public Double getSumOfWeights() {
    return weights.stream().mapToDouble(w -> w).sum();
  }
}
