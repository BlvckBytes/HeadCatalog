package me.blvckbytes.headcatalog.source;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeadModel {

  public final String name;
  public final String skinUrl;
  public final UUID uuid;

  public final Set<String> categories;
  public final Set<String> tags;

  public final Date createdAt;
  public final @Nullable Date updatedAt;

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags, Date createdAt, @Nullable Date updatedAt) {
    this.name = name;
    this.skinUrl = skinUrl;
    this.categories = Collections.unmodifiableSet(categories);
    this.uuid = uuid;
    this.tags = Collections.unmodifiableSet(tags);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags) {
    this(name, skinUrl, categories, uuid, tags, new Date(), null);
  }

  @Override
  public int hashCode() {
    return Objects.hash(normalizeKey(name), normalizeKey(skinUrl));
  }

  private String normalizeKey(String input) {
    return input.trim().toLowerCase(Locale.ROOT);
  }

  @Override
  public String toString() {
    return "HeadModel {\n" +
      "  name='" + name + "',\n" +
      "  skinUrl='" + skinUrl + "',\n" +
      "  uuid='" + uuid + "',\n" +
      "  categories='" + categories + "',\n" +
      "  tags='" + tags + "',\n" +
      "  createdAt='" + createdAt + "',\n" +
      "  updatedAt='" + updatedAt + "'\n" +
    '}';
  }
}
