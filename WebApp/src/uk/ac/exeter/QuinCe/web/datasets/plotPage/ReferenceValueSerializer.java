package uk.ac.exeter.QuinCe.web.datasets.plotPage;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import uk.ac.exeter.QuinCe.utils.DateTimeUtils;
import uk.ac.exeter.QuinCe.utils.DoubleWithUncertainty;

public class ReferenceValueSerializer
  implements JsonSerializer<TreeMap<LocalDateTime, DoubleWithUncertainty>> {

  @Override
  public JsonElement serialize(
    TreeMap<LocalDateTime, DoubleWithUncertainty> src, Type typeOfSrc,
    JsonSerializationContext context) {

    JsonArray array = new JsonArray();

    for (Map.Entry<LocalDateTime, DoubleWithUncertainty> entry : src
      .entrySet()) {
      JsonObject entryJson = new JsonObject();
      entryJson.addProperty("date", DateTimeUtils.dateToLong(entry.getKey()));
      entryJson.addProperty("value", entry.getValue().value());
      array.add(entryJson);
    }

    return array;
  }
}
