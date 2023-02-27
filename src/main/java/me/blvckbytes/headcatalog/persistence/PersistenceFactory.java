package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.headcatalog.config.IPersistenceTypeProvider;

public class PersistenceFactory {

  private final IMySQLCredentialsProvider mySqlCredentialsProvider;
  private final IPersistenceTypeProvider persistenceTypeProvider;
  private final ILogger logger;

  public PersistenceFactory(
    IMySQLCredentialsProvider mySqlCredentialsProvider,
    IPersistenceTypeProvider persistenceTypeProvider,
    ILogger logger
  ) {
    this.mySqlCredentialsProvider = mySqlCredentialsProvider;
    this.persistenceTypeProvider = persistenceTypeProvider;
    this.logger = logger;
  }

  public IPersistence createPersistence() throws Exception {
    EPersistenceType type = persistenceTypeProvider.getPersistenceType();

    if (type == EPersistenceType.MYSQL) {
      MySQLPersistence persistence = new MySQLPersistence(this.mySqlCredentialsProvider, this.logger);
      persistence.connect();
      return persistence;
    }

    throw new IllegalStateException("Unimplemented persistence requested: " + type);
  }
}
