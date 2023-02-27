package me.blvckbytes.headcatalog.persistence;

import me.blvckbytes.headcatalog.apis.HeadModel;

import java.util.Collection;

public interface IPersistence {

  void storeHeadModels(Collection<HeadModel> headModels);

  Collection<HeadModel> loadHeadModels();

}
