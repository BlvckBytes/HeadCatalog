package me.blvckbytes.headcatalog.apis;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class HeadModel {

  public final String name;
  public final String value;
  public final UUID uuid;

  public final @Nullable Set<String> categories;
  public final @Nullable Set<String> tags;

  public HeadModel(String name, String value, @Nullable Set<String> categories, @Nullable UUID uuid, @Nullable Set<String> tags) {
    this.name = name;
    this.value = value;
    this.categories = categories == null ? null : Collections.unmodifiableSet(categories);
    this.uuid = uuid == null ? UUID.randomUUID() : uuid;
    this.tags = tags == null ? null : Collections.unmodifiableSet(tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "HeadModel {\n" +
      "  name='" + name + "',\n" +
      "  value='" + value + "',\n" +
      "  uuid='" + uuid + "',\n" +
      "  categories='" + categories + "',\n" +
      "  tags='" + tags + "'\n" +
    '}';
  }
}
