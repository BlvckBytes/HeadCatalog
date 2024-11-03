package me.blvckbytes.headcatalog;

import com.google.gson.*;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadRegistry {

  // Thanks! :)
  private static final String JSON_FILE_URL = "https://raw.githubusercontent.com/Tweetzy/Data-Files/main/Skulls/skulls.json";
  private static final Gson GSON_INSTANCE = new GsonBuilder().create();

  private final Logger logger;
  private final PlatformScheduler scheduler;
  private final List<Head> heads;

  public HeadRegistry(PlatformScheduler scheduler, Logger logger) {
    this.logger = logger;
    this.scheduler = scheduler;
    this.heads = new ArrayList<>();
  }

  public void load() {
    this.heads.clear();

    scheduler.runAsync(task -> {
      var normalizedCategories = fetchData();

      if (normalizedCategories == null || this.heads.isEmpty())
        return;

      logger.info("Loaded " + heads.size() + " heads into memory");

      Bukkit.getServer().getPluginManager().callEvent(
        new AsyncHeadRegistryLoadEvent(
          Collections.unmodifiableCollection(this.heads),
          Collections.unmodifiableCollection(normalizedCategories)
        )
      );
    });
  }

  private @Nullable List<String> fetchData() {
    try {
      var dataUrl = new URL(JSON_FILE_URL);
      var connection = (HttpURLConnection) dataUrl.openConnection();

      connection.setRequestMethod("GET");
      connection.connect();

      if (connection.getResponseCode() != 200)
        throw new IllegalStateException("Unexpected status-code " + connection.getResponseCode());

      var normalizedCategories = new ArrayList<String>();

      try (
        var inputStreamReader = new InputStreamReader(connection.getInputStream());
        var bufferedReader = new BufferedReader(inputStreamReader)
      ) {
        var jsonData = GSON_INSTANCE.fromJson(bufferedReader, JsonElement.class);

        if (!(jsonData instanceof JsonArray dataArray))
          throw new IllegalStateException("Expected the top-level to be an array, but got " + jsonData.getClass().getSimpleName());

        for (var dataEntry : dataArray) {
          if (!(dataEntry instanceof JsonObject dataObject)) {
            logger.warning("Unexpected non-object entry in array of head-data items: " + dataEntry);
            continue;
          }

          var mappedHead = tryMapHeadFromJson(dataObject, normalizedCategories);

          if (mappedHead == null) {
            logger.warning("Encountered malformed entry in array of head-data items: " + dataEntry);
            continue;
          }

          this.heads.add(mappedHead);
        }
      }

      return normalizedCategories;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to fetch heads from " + JSON_FILE_URL, e);
      return null;
    }
  }

  private @Nullable Head tryMapHeadFromJson(JsonObject data, List<String> existingCategories) {
    if (!(data.get("name") instanceof JsonPrimitive namePrimitive))
      return null;

    if (!(data.get("texture") instanceof JsonPrimitive texturePrimitive))
      return null;

    if (!(data.get("category") instanceof JsonPrimitive categoryPrimitive))
      return null;

    if (!(data.get("tags") instanceof JsonPrimitive tagsPrimitive))
      return null;

    var categoryString = categoryPrimitive.getAsString();
    var normalizedCategoryString = stripNonAlphabeticAndToLowerCase(categoryString);

    int numericCategory;

    if ((numericCategory = existingCategories.indexOf(normalizedCategoryString)) < 0) {
      numericCategory = existingCategories.size();
      existingCategories.add(normalizedCategoryString);
    }

    return new Head(
      namePrimitive.getAsString(),
      categoryString,
      normalizedCategoryString,
      numericCategory,
      splitTags(tagsPrimitive.getAsString()),
      // The file stores textures as URLs, but it's more convenient for rendering to
      // have them accessible as pre-encoded base64 JSON-objects
      base64EncodeTextureUrl(texturePrimitive.getAsString())
    );
  }

  private List<String> splitTags(String tagsString) {
    var result = new ArrayList<String>();

    int tagBeginIndex = 0;

    while (tagBeginIndex < tagsString.length()) {
      var nextCommaIndex = tagsString.indexOf(',', tagBeginIndex);

      String tagValue;

      if (nextCommaIndex < 0)
        tagValue = tagsString.substring(tagBeginIndex);
      else
        tagValue = tagsString.substring(tagBeginIndex, nextCommaIndex);

      result.add(tagValue.trim());

      if (nextCommaIndex < 0)
        break;

      tagBeginIndex = nextCommaIndex + 1;
    }

    return result;
  }

  private String stripNonAlphabeticAndToLowerCase(String input) {
    var result = new StringBuilder();

    for (var i = 0; i < input.length(); ++i) {
      var currentChar = input.charAt(0);

      if (currentChar >= 'A' && currentChar <= 'Z') {
        result.append((char) (currentChar + 32));
        continue;
      }

      if (currentChar >= 'a' && currentChar <= 'z')
        result.append(currentChar);
    }

    return result.toString();
  }

  private String base64EncodeTextureUrl(String textureUrl) {
    var jsonValue = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
    return new String(Base64.getEncoder().encode(jsonValue.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
  }
}
