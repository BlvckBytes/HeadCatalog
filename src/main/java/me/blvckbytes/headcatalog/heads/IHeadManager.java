package me.blvckbytes.headcatalog.heads;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public interface IHeadManager {

  @Nullable Collection<Head> getHeadsUnmodifiable();

  void registerUpdateCallback(Consumer<Collection<Head>> consumer);

  void unregisterUpdateCallback(Consumer<Collection<Head>> consumer);

}
