package me.blvckbytes.headcatalog.persistence;

public interface IMySQLCredentialsProvider {

  String getHost();

  int getPort();

  String getUsername();

  String getPassword();

  String getDatabase();

  String getTable();

}
