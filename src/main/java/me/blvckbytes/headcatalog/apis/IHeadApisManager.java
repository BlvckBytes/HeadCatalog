package me.blvckbytes.headcatalog.apis;

import java.util.Collection;
import java.util.function.Consumer;

public interface IHeadApisManager {

  void registerUpdateCallback(Consumer<Collection<HeadModel>> consumer);

}
