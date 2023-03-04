package me.blvckbytes.headcatalog.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.IFileHandler;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.headcatalog.apis.HeadModel;
import me.blvckbytes.utilitytypes.FUnsafeConsumer;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

public class JsonFilePersistence implements IPersistence {

  private static final String FILE_NAME = "heads.json";

  private final Gson gson;
  private final IFileHandler fileHandler;
  private final ILogger logger;

  private @Nullable PersistenceDataWrapper lastProcessedWrapper;

  public JsonFilePersistence(IFileHandler fileHandler, ILogger logger) {
    this.gson = new GsonBuilder().setPrettyPrinting().create();
    this.fileHandler = fileHandler;
    this.logger = logger;
  }

  @Override
  public void storeHeadModels(Collection<HeadModel> headModels) {
    try {
      openForWriting(outputStream -> {
        this.lastProcessedWrapper = new PersistenceDataWrapper(headModels, System.currentTimeMillis());
        String json = this.gson.toJson(this.lastProcessedWrapper);
        outputStream.write(json.getBytes(StandardCharsets.UTF_8));
      });
    } catch (Exception e) {
      this.logger.log(ELogLevel.ERROR, "An error occurred while trying to store head models to file:");
      this.logger.logError(e);
    }
  }

  @Override
  public long getLastHeadModelsStoreStamp() {
    if (this.lastProcessedWrapper == null)
      loadHeadModels();

    if (this.lastProcessedWrapper == null)
      return 0;

    return this.lastProcessedWrapper.lastStoreStamp;
  }

  @Override
  public Collection<HeadModel> loadHeadModels() {
    if (this.lastProcessedWrapper != null)
      return this.lastProcessedWrapper.heads;

    try {
      if (!this.fileHandler.doesFileExist(FILE_NAME))
        openForWriting(null);

      openForReading(inputStream -> {
        try (
          InputStreamReader streamReader = new InputStreamReader(inputStream);
        ) {
          this.lastProcessedWrapper = this.gson.fromJson(streamReader, PersistenceDataWrapper.class);
          this.logger.log(ELogLevel.INFO, "Read " + this.lastProcessedWrapper.heads.size() + " heads from file");
        }
      });
    } catch (Exception e) {
      this.logger.log(ELogLevel.ERROR, "An error occurred while trying to read head models from file:");
      this.logger.logError(e);
    }

    if (this.lastProcessedWrapper == null)
      return new ArrayList<>();

    return this.lastProcessedWrapper.heads;
  }

  private void openForWriting(@Nullable FUnsafeConsumer<FileOutputStream, Exception> streamConsumer) throws Exception {
    try (
      FileOutputStream outputStream = this.fileHandler.openForWriting(FILE_NAME)
    ) {
      if (outputStream == null)
        throw new IllegalStateException("Could not open the path " + fileHandler.getAbsolutePath(FILE_NAME) + " for writing");

      if (streamConsumer != null)
        streamConsumer.accept(outputStream);
    }
  }

  private void openForReading(@Nullable FUnsafeConsumer<FileInputStream, Exception> streamConsumer) throws Exception {
    try (
      FileInputStream inputStream = this.fileHandler.openForReading(FILE_NAME)
    ) {
      if (inputStream == null)
        throw new IllegalStateException("Could not open the path " + fileHandler.getAbsolutePath(FILE_NAME) + " for reading");

      if (streamConsumer != null)
        streamConsumer.accept(inputStream);
    }
  }

  private static class PersistenceDataWrapper {

    private final Collection<HeadModel> heads;
    private final long lastStoreStamp;

    public PersistenceDataWrapper(Collection<HeadModel> heads, long lastStoreStamp) {
      this.heads = heads;
      this.lastStoreStamp = lastStoreStamp;
    }
  }
}
