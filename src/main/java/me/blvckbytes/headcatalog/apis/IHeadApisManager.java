package me.blvckbytes.headcatalog.apis;

import java.util.Collection;

public interface IHeadApisManager {

  void registerDeltaCallback(FDeltaCallback callback);

  void unregisterDeltaCallback(FDeltaCallback callback);

  void registerHeads(Collection<HeadModel> heads);

  void unregisterHeads(Collection<HeadModel> heads);

  Collection<HeadModel> getHeads();

}
