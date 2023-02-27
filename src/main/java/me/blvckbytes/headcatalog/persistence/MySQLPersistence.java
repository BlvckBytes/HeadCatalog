package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.headcatalog.apis.HeadModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MySQLPersistence implements IPersistence, ICleanable {

  private final IMySQLCredentialsProvider credentialsProvider;
  private final ILogger logger;

  private Connection connection;

  public MySQLPersistence(
    IMySQLCredentialsProvider credentialsProvider,
    ILogger logger
  ) {
    this.credentialsProvider = credentialsProvider;
    this.logger = logger;
  }

  @Override
  public void storeHeadModels(Collection<HeadModel> headModels) {
  }

  @Override
  public Collection<HeadModel> loadHeadModels() {
    List<HeadModel> result = new ArrayList<>();

    try {
      try (
        PreparedStatement statement = this.connection.prepareStatement("")
      ) {
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
          String name = resultSet.getString("name");
          String texturesUrl = resultSet.getString("textures_url");
          UUID uuid = UUID.fromString(resultSet.getString("uuid"));
          Set<String> categories = new HashSet<>(Arrays.asList(resultSet.getString("categories").split(",")));
          Set<String> tags = new HashSet<>(Arrays.asList(resultSet.getString("tags").split(",")));
          result.add(new HeadModel(name, texturesUrl, categories, uuid, tags));
        }
      }
    } catch (Exception e) {
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
      PreparedStatement statement = this.connection.prepareStatement("CREATE DATABASE IF NOT EXISTS `" + database + "`");
    ) {
      statement.executeUpdate();
    }

    try (
      PreparedStatement statement = this.connection.prepareStatement("USE `" + database + "`");
    ) {
      statement.executeUpdate();
    }
  }

  private void createTables() throws Exception {
    String table = credentialsProvider.getTable();

    try (
      PreparedStatement statement = this.connection.prepareStatement(
        "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
          "`name` VARCHAR(255) NOT NULL," +
          "`textures_url` VARCHAR(255) NOT NULL," +
          "`uuid` VARCHAR(255) NOT NULL," +
          "`categories` TEXT NOT NULL," +
          "`tags` TEXT NOT NULL," +
          "PRIMARY KEY(`name`, `value`)" +
        ")"
      )
    ) {
      statement.executeUpdate();
    }
  }
}
