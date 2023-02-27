package me.blvckbytes.headcatalog.source;

import java.util.Collection;
import java.util.function.Consumer;

public interface IHeadApisManager {

  void registerUpdateCallback(Consumer<Collection<HeadModel>> consumer);

}
