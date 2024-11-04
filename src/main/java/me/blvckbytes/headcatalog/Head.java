package me.blvckbytes.headcatalog;

import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Head {

  private static final String MOJANG_TEXTURES_BASE_URL = "http://textures.minecraft.net/texture/";

  private final EvaluationEnvironmentBuilder headEnvironmentBuilder;

  public final String name;
  public final String category;

  // Used for permission-checks
  public final String normalizedCategory;

  // Used to cache permission-check boolean-results
  public final int numericCategory;

  public final Collection<String> tags;
  public final String textureHash;
  public final String base64Textures;

  public Head(
    String name,
    String category,
    String normalizedCategory,
    int numericCategory,
    List<String> tags,
    String textureHash
  ) {
    this.name = name;
    this.category = category;
    this.normalizedCategory = normalizedCategory;
    this.numericCategory = numericCategory;
    this.tags = Collections.unmodifiableCollection(tags);
    this.textureHash = textureHash;
    this.base64Textures = base64EncodeTextureHash(textureHash);

    this.headEnvironmentBuilder = new EvaluationEnvironmentBuilder()
      .withStaticVariable("head_name", name)
      .withStaticVariable("head_category", category)
      .withStaticVariable("head_tags", tags)
      .withStaticVariable("head_base64_textures", base64Textures);
  }

  public EvaluationEnvironmentBuilder getHeadEnvironmentBuilder() {
    return headEnvironmentBuilder.duplicate();
  }

  private static String base64EncodeTextureHash(String hash) {
    var jsonValue = "{\"textures\":{\"SKIN\":{\"url\":\"" + MOJANG_TEXTURES_BASE_URL + hash + "\"}}}";
    return new String(Base64.getEncoder().encode(jsonValue.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
  }

  // When creating unique results, all the user is really interested in are the textures of the head.
  // Thus, as many textures exist multiple times within the data, make their hash be the unique-key.

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Head head)) return false;
    return Objects.equals(textureHash, head.textureHash);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(textureHash);
  }

  @Override
  public String toString() {
    return "Head(name=" + name + ", category=" + category + ", tags=[" + String.join(", ", tags) + "], hash=" + textureHash + ")";
  }
}
