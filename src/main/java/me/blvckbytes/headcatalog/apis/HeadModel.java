package me.blvckbytes.headcatalog.apis;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class HeadModel {

  public final String name;
  public final String textureUrl;
  public final UUID uuid;

  public final Set<String> categories;
  public final Set<String> tags;

  public HeadModel(String name, String textureUrl, Set<String> categories, UUID uuid, Set<String> tags) {
    this.name = name;
    this.textureUrl = textureUrl;
    this.categories = categories;
    this.uuid = uuid;
    this.tags = tags;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, textureUrl);
  }

  @Override
  public String toString() {
    return "HeadModel {\n" +
      "  name='" + name + "',\n" +
      "  textureUrl='" + textureUrl + "',\n" +
      "  uuid='" + uuid + "',\n" +
      "  categories='" + categories + "',\n" +
      "  tags='" + tags + "'\n" +
    '}';
  }
}
