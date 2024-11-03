package me.blvckbytes.headcatalog;

import org.jetbrains.annotations.Nullable;

public record HeadCatalogOpenResult(
  @Nullable EmptyType emptyType,
  int displayedHeadsCount
) {
  public static HeadCatalogOpenResult empty(EmptyType emptyType) {
    return new HeadCatalogOpenResult(emptyType, 0);
  }

  public static HeadCatalogOpenResult ofCount(int count) {
    return new HeadCatalogOpenResult(null, count);
  }
}
