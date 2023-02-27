package me.blvckbytes.headcatalog.config;

import me.blvckbytes.headcatalog.persistence.EPersistenceType;

public interface IPersistenceTypeProvider {

  EPersistenceType getPersistenceType();

}
