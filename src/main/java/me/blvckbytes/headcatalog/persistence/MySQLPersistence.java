package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.headcatalog.source.HeadModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MySQLPersistence implements IPersistence, ICleanable {

  private final IMySQLCredentialsProvider credentialsProvider;
  private final ILogger logger;
  private final String tableName;

  private Connection connection;

  public MySQLPersistence(
    IMySQLCredentialsProvider credentialsProvider,
    ILogger logger
  ) {
    this.credentialsProvider = credentialsProvider;
    this.logger = logger;
    this.tableName = credentialsProvider.getTable();
  }

  @Override
  public void storeHeadModels(Collection<HeadModel> headModels) {
    if (headModels.size() == 0)
      return;

    StringBuilder valuesBuilder = new StringBuilder();

    for (int i = 0; i < headModels.size(); i++) {
      if (i != 0)
        valuesBuilder.append(',');
      valuesBuilder.append("(?, ?, ?, ?, ?, NOW())");
    }

    try {
      try (
        PreparedStatement statement = this.connection.prepareStatement(
          "INSERT INTO `" + tableName + "` (" +
            "`name`," +
            "`textures_url`," +
            "`uuid`," +
            "`categories`," +
            "`tags`," +
            "`created_at`" +
          ") VALUES " + valuesBuilder + " AS `new` " +
          "ON DUPLICATE KEY UPDATE " +
          "`updated_at` = NOW()," +
          "`uuid` = `new`.`uuid`," +
          "`categories` = `new`.`categories`," +
          "`tags` = `new`.`tags`"
        )
      ) {
        int argumentIndex = 1;
        for (HeadModel headModel : headModels) {
          statement.setString(argumentIndex++, headModel.name);
          statement.setString(argumentIndex++, headModel.textureUrl);
          statement.setString(argumentIndex++, headModel.uuid.toString());
          statement.setString(argumentIndex++, String.join(",", headModel.categories));
          statement.setString(argumentIndex++, String.join(",", headModel.tags));
        }
        statement.executeUpdate();
      }
    } catch (Exception e) {
      logger.log(ELogLevel.ERROR, "Could not store head models:");
      logger.logError(e);
    }
  }

  @Override
  public long getLastHeadModelsStoreStamp() {
    try {
      try (
        PreparedStatement statement = this.connection.prepareStatement(
          "SELECT `created_at`, `updated_at`" +
          "FROM `" + tableName + "`" +
          "ORDER BY `updated_at` DESC, `created_at` DESC " +
          "LIMIT 1"
        )
      ) {
        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next())
          return 0;


        Date updatedAt = resultSet.getDate("updated_at");
        if (!resultSet.wasNull())
          return updatedAt.getTime();

        return resultSet.getDate("created_at").getTime();
      }
    } catch (Exception e) {
      logger.log(ELogLevel.ERROR, "Could not read the last update stamp");
      logger.logError(e);
      return 0;
    }
  }

  @Override
  public Collection<HeadModel> loadHeadModels() {
    List<HeadModel> result = new ArrayList<>();

    try {
      try (
        PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM `" + tableName + "`")
      ) {
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          String name = resultSet.getString("name");
          String texturesUrl = resultSet.getString("textures_url");
          UUID uuid = UUID.fromString(resultSet.getString("uuid"));
          Set<String> categories = new HashSet<>(Arrays.asList(resultSet.getString("categories").split(",")));
          Set<String> tags = new HashSet<>(Arrays.asList(resultSet.getString("tags").split(",")));
          Date createdAt = resultSet.getDate("created_at");
          Date updatedAt = resultSet.getDate("updated_at");
          result.add(new HeadModel(name, texturesUrl, categories, uuid, tags, createdAt, updatedAt));
        }
      }
    } catch (Exception e) {
      logger.log(ELogLevel.ERROR, "Could not load head models:");
      logger.logError(e);
    }

    return result;
  }

  protected void connect() throws Exception {
    Class.forName("com.mysql.jdbc.Driver");
    this.connection = DriverManager.getConnection(
      buildConnectionString(),
      this.credentialsProvider.getUsername(),
      this.credentialsProvider.getPassword()
    );

    createAndSelectDatabase();
    createTables();

    logger.log(ELogLevel.INFO, "Successfully connected to the database");
  }

  @Override
  public void cleanup() {
    disconnect();
  }

  private void disconnect() {
    try {
      if (this.connection == null)
        return;

      if (this.connection.isClosed()) {
        this.connection = null;
        return;
      }

      this.connection.close();
      this.connection = null;

      logger.log(ELogLevel.INFO, "Closed connection to the database");
    } catch (Exception e) {
      this.logger.logError(e);
    }
  }

  private String buildConnectionString() {
    return "jdbc:mysql://" + this.credentialsProvider.getHost() + ":" + this.credentialsProvider.getPort() + "?useSSL=false";
  }

  private void createAndSelectDatabase() throws Exception {
    String database = credentialsProvider.getDatabase();

    try (
      PreparedStatement statement = this.connection.prepareStatement("CREATE DATABASE IF NOT EXISTS `" + database + "`")
    ) {
      statement.executeUpdate();
    }

    try (
      PreparedStatement statement = this.connection.prepareStatement("USE `" + database + "`")
    ) {
      statement.executeUpdate();
    }
  }

  private void createTables() throws Exception {
    try (
      PreparedStatement statement = this.connection.prepareStatement(
        "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
          "`name` VARCHAR(255) NOT NULL," +
          "`textures_url` VARCHAR(255) NOT NULL," +
          "`uuid` VARCHAR(255) NOT NULL," +
          "`categories` TEXT NOT NULL," +
          "`tags` TEXT NOT NULL," +
          "`created_at` DATETIME NOT NULL," +
          "`updated_at` DATETIME NULL," +
          "PRIMARY KEY(`name`, `textures_url`)" +
        ")"
      )
    ) {
      statement.executeUpdate();
    }
  }
}
