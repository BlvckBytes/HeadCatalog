package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.headcatalog.apis.HeadModel;

import java.sql.*;
import java.util.*;
import java.util.Date;

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
            "`skin_url`," +
            "`uuid`," +
            "`categories`," +
            "`tags`," +
            "`last_update`" +
          ") VALUES " + valuesBuilder + " AS `new` " +
          "ON DUPLICATE KEY UPDATE " +
          "`last_update` = NOW()," +
          "`uuid` = `new`.`uuid`," +
          "`categories` = `new`.`categories`," +
          "`tags` = `new`.`tags`"
        )
      ) {
        int argumentIndex = 1;
        for (HeadModel headModel : headModels) {
          statement.setString(argumentIndex++, headModel.name);
          statement.setString(argumentIndex++, headModel.skinUrl);
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
          "SELECT `last_update`" +
          "FROM `" + tableName + "`" +
          "ORDER BY `last_update` DESC " +
          "LIMIT 1"
        )
      ) {
        ResultSet resultSet = statement.executeQuery();

        if (!resultSet.next())
          return 0;

        return resultSet.getDate("last_update").getTime();
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
          String skinUrl = resultSet.getString("skin_url");
          UUID uuid = UUID.fromString(resultSet.getString("uuid"));
          Set<String> categories = new HashSet<>(Arrays.asList(resultSet.getString("categories").split(",")));
          Set<String> tags = new HashSet<>(Arrays.asList(resultSet.getString("tags").split(",")));
          Date lastUpdate = resultSet.getTimestamp("last_update");
          result.add(new HeadModel(name, skinUrl, categories, uuid, tags, lastUpdate));
        }
      }
    } catch (Exception e) {
      logger.log(ELogLevel.ERROR, "Could not load head models:");
      logger.logError(e);
    }

    return result;
  }

  protected void connect() throws Exception {
    Class.forName("com/mysql/cj/jdbc/Driver".replace('/', '.'));

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
          "`skin_url` VARCHAR(255) NOT NULL," +
          "`uuid` VARCHAR(255) NOT NULL," +
          "`categories` TEXT NOT NULL," +
          "`tags` TEXT NOT NULL," +
          "`last_update` DATETIME NOT NULL," +
          "PRIMARY KEY(`name`, `skin_url`)" +
        ")"
      )
    ) {
      statement.executeUpdate();
    }
  }
}
