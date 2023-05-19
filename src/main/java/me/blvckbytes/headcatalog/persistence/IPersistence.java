package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.headcatalog.apis.HeadModel;

import java.util.Set;

public interface IPersistence {

  void storeHeadModels(Set<HeadModel> headModels);

  long getLastHeadModelsStoreStamp();

  Set<HeadModel> loadHeadModels();

}
