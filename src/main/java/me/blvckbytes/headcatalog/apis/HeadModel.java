package me.blvckbytes.headcatalog.apis;

import java.util.*;

public class HeadModel {

  public final String name;
  public final String skinUrl;
  public final UUID uuid;

  public final Set<String> categories;
  public final Set<String> tags;

  public Date lastUpdate;

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags, Date lastUpdate) {
    this.name = name;
    this.skinUrl = skinUrl;
    this.categories = Collections.unmodifiableSet(categories);
    this.uuid = uuid;
    this.tags = Collections.unmodifiableSet(tags);
    this.lastUpdate = lastUpdate;
  }

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags) {
    this(name, skinUrl, categories, uuid, tags, new Date());
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
      "  lastUpdate='" + lastUpdate + "'\n" +
    '}';
  }
}
