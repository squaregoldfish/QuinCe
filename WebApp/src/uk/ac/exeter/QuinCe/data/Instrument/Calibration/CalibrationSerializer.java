package uk.ac.exeter.QuinCe.data.Instrument.Calibration;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import uk.ac.exeter.QuinCe.utils.DateTimeUtils;

public class CalibrationSerializer implements JsonSerializer<Calibration> {

  @Override
  public JsonElement serialize(Calibration src, Type typeOfSrc,
    JsonSerializationContext context) {

    JsonObject json = new JsonObject();

    json.addProperty("id", src.getId());
    json.addProperty("deploymentDate",
      DateTimeUtils.formatDateTime(src.getDeploymentDate()));
    json.addProperty("target", src.getTarget());
    json.addProperty("humanReadableCoefficients",
      src.getHumanReadableCoefficients());

    JsonArray coefficients = new JsonArray();

    for (CalibrationCoefficient coefficient : src.getCoefficients()) {
      JsonObject coefficientJson = new JsonObject();
      coefficientJson.addProperty("name", coefficient.getName());
      coefficientJson.addProperty("type", coefficient.getType());
      coefficientJson.addProperty("value", coefficient.getValue());

      coefficients.add(coefficientJson);
    }

    json.add("coefficients", coefficients);

    return json;
  }

}
