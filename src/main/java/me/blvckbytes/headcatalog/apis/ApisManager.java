package me.blvckbytes.headcatalog.apis;

import com.google.gson.*;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.MakeHeadFunction;
import me.blvckbytes.utilitytypes.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ApisManager implements IInitializable {

  private final AExpressionFunction makeHeadFunction;
  private final IHeadApisProvider headApisProvider;
  private final JsonParser jsonParser;
  private final Plugin plugin;
  private final ILogger logger;

  public ApisManager(
    ILogger logger,
    Plugin plugin,
    IHeadApisProvider headApisProvider
  ) {
    this.headApisProvider = headApisProvider;
    this.plugin = plugin;
    this.logger = logger;

    this.jsonParser = new JsonParser();
    this.makeHeadFunction = new MakeHeadFunction(logger);
  }

  @Override
  public void initialize() {
    fetchHeadApis(result -> {
      logger.log(ELogLevel.INFO, "Fetched " + result.size() + " head models! :)");

      for (HeadModel headModel : result)
        System.out.println(headModel);
    });
  }

  private void fetchHeadApis(Consumer<List<HeadModel>> completion) {
    List<HeadModel> result = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger fetchResultCounter = new AtomicInteger(0);

    List<? extends IHeadApi> apis = this.headApisProvider.getApis();

    if (apis.size() == 0) {
      completion.accept(result);
      return;
    }

    for (IHeadApi headApi : apis)
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        List<HeadModel> fetchResult = this.fetchHeadApi(headApi);

        if (fetchResult != null)
          result.addAll(fetchResult);

        if (fetchResultCounter.incrementAndGet() == apis.size())
          completion.accept(result);
      });
  }

  private @Nullable List<HeadModel> fetchHeadApi(IHeadApi headApi) {
    try {
      URL url = new URL(headApi.getUrl());
      Tuple<Integer, @Nullable String> result = performGetRequest(url);

      if (result.b == null)
        throw new IllegalStateException("API request to " + url + " failed (" + result.a + ")!");

      return parseHeadApiResult(headApi, result.b);
    } catch (Exception e) {
      this.logger.logError(e);
      return null;
    }
  }

  private @Nullable List<HeadModel> parseHeadApiResult(IHeadApi headApi, String resultBody) {
    EApiDataType dataType = headApi.getDataType().asEnumerationConstant(EApiDataType.class, GPEEE.EMPTY_ENVIRONMENT);

    if (dataType == EApiDataType.JSON)
      return mapHeadApiJsonResult(headApi, this.jsonParser.parse(resultBody));

    throw new IllegalStateException("Unknown API data type: " + dataType);
  }

  private Object convertJsonElementToObject(JsonElement element) {
    if (element.isJsonNull())
      return null;

    if (element.isJsonObject())
      return convertJsonObjectToMap(element.getAsJsonObject());

    if (element.isJsonArray())
      return convertJsonArrayToList(element.getAsJsonArray());

    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();

      if (primitive.isString())
        return primitive.getAsString();

      if (primitive.isNumber())
        return primitive.getAsNumber();

      if (primitive.isBoolean())
        return primitive.getAsBoolean();

      throw new IllegalStateException("Unknown JSON primitive encountered");
    }

    throw new IllegalStateException("Unknown JSON element encountered");
  }

  private List<Object> convertJsonArrayToList(JsonArray array) {
    List<Object> result = new ArrayList<>();

    for (JsonElement element : array)
      result.add(convertJsonElementToObject(element));

    return result;
  }

  private Map<String, Object> convertJsonObjectToMap(JsonObject object) {
    Map<String, Object> result = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : object.entrySet())
      result.put(entry.getKey(), convertJsonElementToObject(entry.getValue()));

    return result;
  }

  private @Nullable List<HeadModel> mapHeadApiJsonResult(IHeadApi api, JsonElement json) {
    Object result = convertJsonElementToObject(json);
    IEvaluationEnvironment extractorEnvironment = getApiResultExtractorEnvironment(result);
    Object extractedArray = api.getArrayExtractor().asRawObject(extractorEnvironment);

    if (!(extractedArray instanceof Collection)) {
      logger.log(ELogLevel.ERROR, "The array extractor of " + api.getUrl() + " didn't yield a collection");
      return null;
    }

    List<HeadModel> mappedHeads = new ArrayList<>();
    for (Object item : (List<?>) extractedArray) {
      IEvaluationEnvironment mapperEnvironment = getApiResultMappingEnvironment(item);
      Object mappedItem = api.getItemMapper().asRawObject(mapperEnvironment);

      if (!(mappedItem instanceof HeadModel)) {
        logger.log(ELogLevel.ERROR, "The item mapper of " + api.getUrl() + " didn't yield a HeadModel");
        return null;
      }

      mappedHeads.add((HeadModel) mappedItem);
    }

    return mappedHeads;
  }

  private IEvaluationEnvironment getApiResultExtractorEnvironment(Object result) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("result", result)
      .build();
  }

  private IEvaluationEnvironment getApiResultMappingEnvironment(Object item) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("item", item)
      .withFunction("make_head", makeHeadFunction)
      .build();
  }

  private Tuple<Integer, @Nullable String> performGetRequest(URL url) throws Exception {
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();

    int code = connection.getResponseCode();
    if (code != 200)
      return new Tuple<>(code, null);

    try (
      InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
      BufferedReader bufferedReader = new BufferedReader(streamReader)
    ) {
      String body = bufferedReader.lines().collect(Collectors.joining());
      return new Tuple<>(code, body);
    }
  }
}
