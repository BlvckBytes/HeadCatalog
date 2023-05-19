package me.blvckbytes.headcatalog.apis;

import com.google.gson.*;
import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.MakeHeadFunction;
import me.blvckbytes.headcatalog.persistence.IPersistence;
import me.blvckbytes.utilitytypes.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HeadApisManager implements IHeadApisManager, IInitializable, ICleanable {

  private static final long UPDATE_CHECKER_PERIOD_T = 20 * 60;

  // Smallest update period we'll allow is going to be 24 hours
  private static final long API_UPDATE_PERIOD_S_MIN = 60 * 60 * 24;

  private final AExpressionFunction makeHeadFunction;
  private final IHeadApisProvider headSourceProvider;
  private final JsonParser jsonParser;
  private final Plugin plugin;
  private final Logger logger;
  private final IPersistence persistence;

  private final Set<FDeltaCallback> deltaCallbacks;

  private @Nullable BukkitTask updateTask;
  private final long updatePeriod;

  private final Set<HeadModel> headModels;
  private long lastFetchedLastStoreStamp;

  public HeadApisManager(
    Logger logger,
    Plugin plugin,
    IHeadApisProvider headSourceProvider,
    IPersistence persistence
  ) {
    this.headSourceProvider = headSourceProvider;
    this.persistence = persistence;
    this.plugin = plugin;
    this.logger = logger;

    this.headModels = new HashSet<>();
    this.deltaCallbacks = new HashSet<>();
    this.jsonParser = new JsonParser();
    this.makeHeadFunction = new MakeHeadFunction(logger);
    this.updatePeriod = Math.max(API_UPDATE_PERIOD_S_MIN, this.headSourceProvider.getUpdatePeriodSeconds());
  }

  private void checkForUpdates() {
    long lastStoreStamp = persistence.getLastHeadModelsStoreStamp();

    // Update period elapsed, fetch new data from APIs
    if (System.currentTimeMillis() - lastStoreStamp >= updatePeriod * 1000) {
      fetchHeadApis(result -> {
        logger.log(Level.INFO, "Fetched " + result.size() + " heads from APIs");

        // API updates should never remove, only extend
        integrateUpdate(result, false);

        // Store the local state after integrating the update
        persistence.storeHeadModels(this.headModels);
      });

      return;
    }

    // The database holds newer values then currently loaded into memory
    if (this.headModels.isEmpty() || lastFetchedLastStoreStamp < lastStoreStamp) {
      lastFetchedLastStoreStamp = lastStoreStamp;
      integrateUpdate(persistence.loadHeadModels(), false);
    }
  }

  private void notifyOfDelta(Collection<HeadModel> delta, EDeltaMode mode) {
    if (delta.size() == 0)
      return;

    for (FDeltaCallback callback : deltaCallbacks)
      callback.accept(delta, mode);
  }

  private void integrateUpdate(Set<HeadModel> heads, boolean applyRemoved) {
    List<HeadModel> added = new ArrayList<>();

    // NOTE: This collection is also used to remove wrapped entries in other
    // collections, so using a set is serving double and should not be changed
    Set<HeadModel> removed = new HashSet<>(headModels);

    for (HeadModel head : heads) {
      if (this.headModels.add(head))
        added.add(head);

      // Starts off with all currently existing elements
      // Elements present after the update are removed
      // => Remaining elements have been removed by the update
      if (applyRemoved)
        removed.remove(head);
    }

    if (applyRemoved) {
      this.headModels.removeAll(removed);
      notifyOfDelta(removed, EDeltaMode.REMOVED);
    }

    notifyOfDelta(added, EDeltaMode.ADDED);
  }

  @Override
  public void initialize() {
    this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
      this.plugin,
      this::checkForUpdates,
      0L, UPDATE_CHECKER_PERIOD_T
    );
  }

  @Override
  public void cleanup() {
    if (this.updateTask != null)
      this.updateTask.cancel();
  }

  private void fetchHeadApis(Consumer<Set<HeadModel>> completion) {
    Set<HeadModel> result = Collections.synchronizedSet(new HashSet<>());
    AtomicInteger entriesCounter = new AtomicInteger(0);

    List<? extends IHeadApi> apis = this.headSourceProvider.getApis();

    if (apis.size() == 0) {
      completion.accept(result);
      return;
    }

    for (IHeadApi headApi : apis)
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        this.fetchHeadApiUrls(headApi, result);

        if (entriesCounter.incrementAndGet() == apis.size())
          completion.accept(result);
      });
  }

  private void fetchHeadApiUrls(IHeadApi headApi, Collection<HeadModel> result) {
    List<String> urlStrings = headApi.getUrls();

    for (String urlString : urlStrings) {
      List<HeadModel> fetchResult = fetchHeadApiUrl(headApi, urlString);

      if (fetchResult != null)
        result.addAll(fetchResult);
    }
  }

  private @Nullable List<HeadModel> fetchHeadApiUrl(IHeadApi headApi, String urlString) {
    try {
      URL url = new URL(urlString);
      Tuple<Integer, @Nullable String> result = performGetRequest(url, headApi.getUserAgent());

      if (result.b == null)
        throw new IllegalStateException("API request to " + url + " failed (" + result.a + ")!");

      return parseHeadApiResult(headApi, urlString, result.b);
    } catch (Exception e) {
      this.logger.log(Level.SEVERE, e, () -> "Could not fetch heads from API url");
      return null;
    }
  }

  private @Nullable List<HeadModel> parseHeadApiResult(IHeadApi headApi, String urlString, String resultBody) {
    EApiDataType dataType = headApi.getDataType().asEnumerationConstant(EApiDataType.class, GPEEE.EMPTY_ENVIRONMENT);

    if (dataType == EApiDataType.JSON)
      return mapHeadApiJsonResult(headApi, urlString, this.jsonParser.parse(resultBody));

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

  private @Nullable List<HeadModel> mapHeadApiJsonResult(IHeadApi api, String urlString, JsonElement json) {
    Object result = convertJsonElementToObject(json);
    IEvaluationEnvironment extractorEnvironment = getApiResultExtractorEnvironment(result);
    Object extractedArray = api.getArrayExtractor().asRawObject(extractorEnvironment);

    if (!(extractedArray instanceof Collection)) {
      logger.log(Level.SEVERE, "The array extractor of " + urlString + " didn't yield a collection");
      return null;
    }

    List<HeadModel> mappedHeads = new ArrayList<>();
    for (Object item : (List<?>) extractedArray) {
      IEvaluationEnvironment mapperEnvironment = getApiResultMappingEnvironment(item, urlString);
      Object mappedItem = api.getItemMapper().asRawObject(mapperEnvironment);

      if (!(mappedItem instanceof HeadModel)) {
        logger.log(Level.SEVERE, "The item mapper of " + urlString + " didn't yield a HeadModel");
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

  private IEvaluationEnvironment getApiResultMappingEnvironment(Object item, String url) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("item", item)
      .withStaticVariable("url", url)
      .withFunction("make_head", makeHeadFunction)
      .build();
  }

  private Tuple<Integer, @Nullable String> performGetRequest(URL url, String userAgent) throws Exception {
    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", userAgent);
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

  @Override
  public void registerDeltaCallback(FDeltaCallback callback) {
    this.deltaCallbacks.add(callback);
  }

  @Override
  public void unregisterDeltaCallback(FDeltaCallback callback) {
    this.deltaCallbacks.remove(callback);
  }

  @Override
  public void registerHeads(Collection<HeadModel> heads) {
    Set<HeadModel> temporary = new HashSet<>(this.headModels);
    temporary.addAll(heads);
    integrateUpdate(temporary, false);
  }

  @Override
  public void unregisterHeads(Collection<HeadModel> heads) {
    Set<HeadModel> temporary = new HashSet<>(this.headModels);
    temporary.removeAll(heads);
    integrateUpdate(temporary, true);
  }

  @Override
  public Collection<HeadModel> getHeads() {
    return new ArrayList<>(this.headModels);
  }
}
