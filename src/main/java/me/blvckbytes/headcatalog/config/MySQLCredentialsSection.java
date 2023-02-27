package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import org.jetbrains.annotations.Nullable;

public class MySQLCredentialsSection implements IConfigSection {

  protected String host;
  protected int port;
  protected String username;
  protected String password;
  protected String database;
  protected String table;

  @Override
  public @Nullable Object defaultFor(Class<?> type, String field) {
    switch (field) {
      case "host":
        return "localhost";

      case "port":
        return 3306;

      case "username":
        return "root";

      case "password":
        return "";

      case "database":
        return "head_catalog";

      case "table":
        return "head_model";

      default:
        return null;
    }
  }
}
