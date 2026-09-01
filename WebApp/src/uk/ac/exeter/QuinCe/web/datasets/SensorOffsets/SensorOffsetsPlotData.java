package uk.ac.exeter.QuinCe.web.datasets.SensorOffsets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;

import uk.ac.exeter.QuinCe.data.Dataset.DatasetSensorValues;
import uk.ac.exeter.QuinCe.data.Dataset.SensorOffsets;
import uk.ac.exeter.QuinCe.data.Dataset.SensorOffsetsException;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValue;
import uk.ac.exeter.QuinCe.data.Dataset.SensorValuesList;
import uk.ac.exeter.QuinCe.data.Instrument.SensorDefinition.SensorGroupPair;
import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

/**
 * Data structure for building main time series plot data for the Sensor Offsets
 * page.
 */
public class SensorOffsetsPlotData {

  /**
   * The internal data structure.
   */
  private TreeMap<LocalDateTime, Tuple> data;

  private List<SensorValue> series2;

  private DatasetSensorValues allSensorValues;

  protected SensorOffsetsPlotData(SensorValuesList series1Points,
    SensorValuesList series2Points, DatasetSensorValues allSensorValues) {

    this.allSensorValues = allSensorValues;

    data = new TreeMap<LocalDateTime, Tuple>();

    // Extract the two series into the Tuple map.
    processSeries1(series1Points);
    processSeries2(series2Points);

    // Store the second series separately - we need it to apply offsets to.
    this.series2 = series2Points.getRawValues();
  }

  private void processSeries1(SensorValuesList points) {
    points.getRawValues().stream()
      .filter(
        p -> allSensorValues.getFlagScheme().isGood(p.getUserQCFlag(), true))
      .forEach(p -> {
        if (!p.getDoubleValue().isNaN()) {
          Tuple tuple = new Tuple();
          tuple.setFirst(p.getDoubleValue());
          data.put(p.getCoordinate().getTime(), tuple);
        }
      });
  }

  private void processSeries2(SensorValuesList points) {
    points.getRawValues().stream()
      .filter(
        p -> allSensorValues.getFlagScheme().isGood(p.getUserQCFlag(), true))
      .forEach(p -> {
        if (!p.getDoubleValue().isNaN()) {
          if (data.containsKey(p.getCoordinate().getTime())) {
            data.get(p.getCoordinate().getTime()).setSecond(p.getDoubleValue());
          } else {
            Tuple tuple = new Tuple();
            tuple.setSecond(p.getDoubleValue());
            data.put(p.getCoordinate().getTime(), tuple);
          }
        }
      });
  }

  protected String getArray(SensorOffsets sensorOffsets,
    SensorGroupPair groupPair) throws SensorOffsetsException {

    List<SensorValue> offsetsApplied = sensorOffsets.applyOffsets(groupPair,
      series2, allSensorValues);

    TreeMap<LocalDateTime, Tuple> dataWithOffset = new TreeMap<LocalDateTime, Tuple>(
      data);

    // Add the offsets to the copied map
    offsetsApplied.forEach(o -> {
      if (dataWithOffset.containsKey(o.getCoordinate().getTime())) {

        Tuple oldTuple = dataWithOffset.get(o.getCoordinate().getTime());

        Tuple newTuple = new Tuple(oldTuple);
        newTuple.setOffsetSecond(o.getDoubleValue());
        dataWithOffset.put(o.getCoordinate().getTime(), newTuple);
      } else {
        Tuple tuple = new Tuple();
        tuple.setOffsetSecond(o.getDoubleValue());
        dataWithOffset.put(o.getCoordinate().getTime(), tuple);
      }
    });

    JsonArray json = new JsonArray();

    for (Map.Entry<LocalDateTime, Tuple> entry : dataWithOffset.entrySet()) {
      JsonArray entryArray = new JsonArray();
      entryArray.add(DateTimeUtils.dateToLong(entry.getKey()));

      if (entry.getValue().getFirst().isNaN()) {
        entryArray.add(JsonNull.INSTANCE);
      } else {
        entryArray.add(entry.getValue().getFirst().value());
      }

      if (entry.getValue().getSecond().isNaN()) {
        entryArray.add(JsonNull.INSTANCE);
      } else {
        entryArray.add(entry.getValue().getSecond().value());
      }

      if (entry.getValue().getOffsetSecond().isNaN()) {
        entryArray.add(JsonNull.INSTANCE);
      } else {
        entryArray.add(entry.getValue().getOffsetSecond().value());
      }

      json.add(entryArray);
    }

    return json.toString();
  }

  protected Double getFirstSeriesValue(LocalDateTime time) {
    Tuple tuple = data.get(time);
    return null == tuple ? Double.NaN : tuple.getFirst().value();
  }

  protected Double getSecondSeriesValue(LocalDateTime time) {
    Tuple tuple = data.get(time);
    return null == tuple ? Double.NaN : tuple.getSecond().value();
  }

  /**
   * A simple Tuple of two Double values
   */
  private class Tuple {

    private DoubleWithUncertainty first = DoubleWithUncertainty.NaN;

    private DoubleWithUncertainty second = DoubleWithUncertainty.NaN;

    private DoubleWithUncertainty offsetSecond = DoubleWithUncertainty.NaN;

    protected Tuple() {
      // Blank constructor
    }

    protected Tuple(Tuple source) {
      this.first = source.first;
      this.second = source.second;
      this.offsetSecond = source.offsetSecond;
    }

    protected void setFirst(DoubleWithUncertainty first) {
      this.first = first;
    }

    protected void setSecond(DoubleWithUncertainty second) {
      this.second = second;
    }

    protected void setOffsetSecond(DoubleWithUncertainty offsetSecond) {
      this.offsetSecond = offsetSecond;
    }

    private DoubleWithUncertainty getFirst() {
      return first;
    }

    private DoubleWithUncertainty getSecond() {
      return second;
    }

    private DoubleWithUncertainty getOffsetSecond() {
      return offsetSecond;
    }
  }
}
