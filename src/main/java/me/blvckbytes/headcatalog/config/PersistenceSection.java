package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.headcatalog.persistence.EPersistenceType;
import me.blvckbytes.headcatalog.persistence.IMySQLCredentialsProvider;

import java.lang.reflect.Field;
import java.util.List;

public class PersistenceSection implements IConfigSection, IMySQLCredentialsProvider, IPersistenceTypeProvider {

  @CSAlways
  private MySQLCredentialsSection mySqlCredentials;

  private BukkitEvaluable type;

  @CSIgnore
  private EPersistenceType persistenceType;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    this.persistenceType = type.asEnumerationConstant(EPersistenceType.class, GPEEE.EMPTY_ENVIRONMENT);
  }

  @Override
  public String getHost() {
    return this.mySqlCredentials.host;
  }

  @Override
  public int getPort() {
    return this.mySqlCredentials.port;
  }

  @Override
  public String getUsername() {
    return this.mySqlCredentials.username;
  }

  @Override
  public String getPassword() {
    return this.mySqlCredentials.password;
  }

  @Override
  public String getDatabase() {
    return this.mySqlCredentials.database;
  }

  @Override
  public String getTable() {
    return this.mySqlCredentials.table;
  }

  @Override
  public EPersistenceType getPersistenceType() {
    return this.persistenceType;
  }
}
