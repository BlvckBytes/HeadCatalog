package me.blvckbytes.headcatalog.source;

import java.util.Collection;
import java.util.function.Consumer;

public interface IHeadManager {

  Collection<HeadModel> getHeads();

  void registerUpdateCallback(Consumer<Collection<HeadModel>> consumer);

}
