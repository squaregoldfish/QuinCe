package uk.ac.exeter.QuinCe.utils;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DoubleWithUncertaintySerializer
  implements JsonSerializer<DoubleWithUncertainty>,
  JsonDeserializer<DoubleWithUncertainty> {

  private static final Double NAN_DOUBLE = -9999.9D;

  private static final Float NAN_FLOAT = -9999.9F;

  @Override
  public JsonElement serialize(DoubleWithUncertainty arg0, Type arg1,
    JsonSerializationContext arg2) {

    JsonArray element = new JsonArray();

    if (arg0.isNaN()) {
      element.add(NAN_DOUBLE);
    } else {
      element.add(arg0.value());
    }

    if (!arg0.hasUncertainty()) {
      element.add(NAN_FLOAT);
    } else {
      element.add(arg0.uncertainty());
    }

    return element;
  }

  @Override
  public DoubleWithUncertainty deserialize(JsonElement arg0, Type arg1,
    JsonDeserializationContext arg2) throws JsonParseException {

    JsonArray array = arg0.getAsJsonArray();

    Double value = array.get(0).getAsDouble();
    Float uncertainty = array.get(1).getAsFloat();

    if (value == NAN_DOUBLE) {
      value = Double.NaN;
    }

    if (uncertainty == NAN_FLOAT) {
      uncertainty = Float.NaN;
    }

    return new DoubleWithUncertainty(value, uncertainty);
  }
}
