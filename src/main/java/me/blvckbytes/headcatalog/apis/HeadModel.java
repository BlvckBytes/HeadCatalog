package me.blvckbytes.headcatalog.apis;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HeadModel implements Comparable<HeadModel> {

  public final String name;
  public final String skinUrl;
  public final UUID uuid;

  public final Set<String> categories;
  public final String categoriesString;
  public final Set<String> tags;
  public final String tagsString;

  public final double price;

  public Date lastUpdate;

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags, double price, Date lastUpdate) {
    this.name = name;
    this.skinUrl = skinUrl;

    this.categories = Collections.unmodifiableSet(categories);
    this.categoriesString = String.join(" ", this.categories);

    this.uuid = uuid;

    this.tags = Collections.unmodifiableSet(tags);
    this.tagsString = String.join(" ", this.tags);

    this.price = price;

    this.lastUpdate = lastUpdate;
  }

  public HeadModel(String name, String skinUrl, Set<String> categories, UUID uuid, Set<String> tags, double price) {
    this(name, skinUrl, categories, uuid, tags, price, new Date());
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

  @Override
  public int compareTo(@NotNull HeadModel o) {
    int result;

    if ((result = name.compareTo(o.name)) != 0)
      return result;

    if ((result = categoriesString.compareTo(o.categoriesString)) != 0)
      return result;

    return tagsString.compareTo(o.tagsString);
  }
}
